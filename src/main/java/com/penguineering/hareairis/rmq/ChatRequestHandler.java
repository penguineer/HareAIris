package com.penguineering.hareairis.rmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penguineering.hareairis.ai.AIChatService;
import com.penguineering.hareairis.model.ChatError;
import com.penguineering.hareairis.model.ChatRequest;
import com.penguineering.hareairis.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handles chat requests from RabbitMQ.
 *
 * <p>Handles chat requests from RabbitMQ, processes them using the AiChatService and sends the response back to the
 * replyTo queue.</p>
 */
@Component
public class ChatRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatRequestHandler.class);
    private final ObjectMapper objectMapper;
    private final AIChatService aiChatService;
    private final RabbitTemplate rabbitTemplate;

    public ChatRequestHandler(ObjectMapper objectMapper,
                              AIChatService aiChatService,
                              RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.aiChatService = aiChatService;
        this.rabbitTemplate = rabbitTemplate;
    }


    /**
     * Handles a chat request.
     *
     * <p>Handles a chat request, processes it using the AiChatService and sends the response back to the replyTo queue.</p>
     *
     * @param message The chat request message.
     */
    public void handleMessage(Message message) {
        // Extract the correlation ID
        Optional<String> correlationId = Optional
                .ofNullable(message.getMessageProperties())
                .map(MessageProperties::getCorrelationId);
        correlationId.ifPresentOrElse(
                id -> logger.info("Received a chat request with Correlation ID: {}", id),
                () -> logger.warn("Received a chat request without Correlation ID")
        );

        // Extract the custom error queue header
        Optional<String> errorTo = Optional
                .ofNullable(message.getMessageProperties())
                .map(props -> props.getHeader("error_to"))
                .map(String.class::cast);
        if (errorTo.isEmpty())
            logger.warn("Error_to header not provided, errors will be logged only!");

        try {
            logger.info("Received message: {}", new String(message.getBody()));
            ChatRequest chatRequest = deserializeChatRequest(message);

            // Extract the "reply_to" property
            String replyTo = Optional
                    .ofNullable(message.getMessageProperties())
                    .map(MessageProperties::getReplyTo)
                    .orElseThrow(() -> new ChatError(ChatError.Code.CODE_BAD_REQUEST, "Reply_to property is missing"));
            logger.info("Reply-to header: {}", replyTo);


            ChatResponse result = aiChatService.handleChatRequest(chatRequest);

            // Convert ChatResponse to JSON
            String jsonResponse = serializeChatResponse(result);

            // Send the response to the replyTo queue
            MessageProperties messageProperties = new MessageProperties();
            correlationId.ifPresent(messageProperties::setCorrelationId);
            messageProperties.setContentType("application/json");
            Message responseMessage = new Message(jsonResponse.getBytes(), messageProperties);
            rabbitTemplate.send(replyTo, responseMessage);
        } catch (Exception e) {
            Optional<String> json = serializeChatError(e);
            errorTo.ifPresentOrElse(
                    to -> json.ifPresent(
                            j -> rabbitTemplate.convertAndSend(to, j)),
                    () -> logger.error("Error on handling chat request!", e)
            );
        }
    }

    private ChatRequest deserializeChatRequest(Message message) throws ChatError {
        try {
            return objectMapper.readValue(message.getBody(), ChatRequest.class);
        } catch (Exception e) {
            throw new ChatError(ChatError.Code.CODE_BAD_REQUEST,
                    "Failed to deserialize chat request: " + e.getMessage());
        }
    }

    private String serializeChatResponse(ChatResponse response) throws ChatError {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Failed to serialize chat response", e);
            throw new ChatError(ChatError.Code.CODE_INTERNAL_SERVER_ERROR,
                    "Failed to serialize chat response: " + e.getMessage());
        }
    }

    private Optional<String> serializeChatError(Exception e) {
        Optional<ChatError> error = e instanceof ChatError
                ? Optional.of((ChatError) e)
                : Optional.of(new ChatError(e.getMessage()));

        try {
            return objectMapper.writeValueAsString(error).describeConstable();
        } catch (Exception ex) {
            logger.error("Failed to serialize error", ex);
            return Optional.empty();
        }

    }
}