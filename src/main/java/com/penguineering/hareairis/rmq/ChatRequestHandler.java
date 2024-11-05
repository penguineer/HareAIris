package com.penguineering.hareairis.rmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penguineering.hareairis.ai.AIChatService;
import com.penguineering.hareairis.model.ChatError;
import com.penguineering.hareairis.model.ChatException;
import com.penguineering.hareairis.model.ChatRequest;
import com.penguineering.hareairis.model.ChatResponse;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Handles chat requests from RabbitMQ.
 *
 * <p>Handles chat requests from RabbitMQ, processes them using the AiChatService and sends the response back to the
 * replyTo queue.</p>
 */
@Component
public class ChatRequestHandler implements ChannelAwareMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ChatRequestHandler.class);
    private final ObjectMapper objectMapper;
    private final AIChatService aiChatService;
    private final RabbitTemplate rabbitTemplate;
    private final RateLimitGate rateLimitGate;

    public ChatRequestHandler(ObjectMapper objectMapper,
                              AIChatService aiChatService,
                              RabbitTemplate rabbitTemplate,
                              RateLimitGate rateLimitGate) {
        this.objectMapper = objectMapper;
        this.aiChatService = aiChatService;
        this.rabbitTemplate = rabbitTemplate;
        this.rateLimitGate = rateLimitGate;
    }


    /**
     * Handles a chat request.
     *
     * <p>Handles a chat request, processes it using the AiChatService and sends the response back to the replyTo queue.</p>
     *
     * @param message The chat request message.
     */
    @Override
    public void onMessage(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

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
            ChatRequest chatRequest = deserializeChatRequest(message);

            // Extract the "reply_to" property
            String replyTo = Optional
                    .ofNullable(message.getMessageProperties())
                    .map(MessageProperties::getReplyTo)
                    .orElseThrow(() -> new ChatException(ChatException.Code.CODE_BAD_REQUEST, "Reply_to property is missing"));
            logger.info("Reply-to header: {}", replyTo);


            ChatResponse result = rateLimitGate.callWithRateLimit(
                    () -> aiChatService.handleChatRequest(chatRequest));

            // Convert ChatResponse to JSON
            String jsonResponse = serializeChatResponse(result);

            // Send the response to the replyTo queue
            MessageProperties messageProperties = new MessageProperties();
            correlationId.ifPresent(messageProperties::setCorrelationId);
            messageProperties.setContentType("application/json");
            Message responseMessage = new Message(jsonResponse.getBytes(), messageProperties);
            rabbitTemplate.send(replyTo, responseMessage);

            // Acknowledge the message
            channel.basicAck(deliveryTag, false);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for rate limit, current message will not be acked and remains in the queue.");

            // restore the interrupt flag
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.info("Error on chat request", e);
            Optional<String> json = serializeChatError(e);
            errorTo.ifPresentOrElse(
                    to -> json.ifPresent(
                            j -> rabbitTemplate.convertAndSend(to, j)),
                    () -> logger.error("Error on handling chat request!", e)
            );

            doExceptionBasedAck(e, channel, deliveryTag);
        }
    }

    private void doExceptionBasedAck(Exception e, Channel channel, long deliveryTag) {
        try {
            if (e instanceof ChatException chatException)
                if (chatException.is5xxServerError())
                    // Do not acknowledge the message
                    channel.basicNack(deliveryTag, false, true);

            // Acknowledge the message
            channel.basicAck(deliveryTag, false);
        } catch (IOException ex) {
            logger.error("Failed send message (n)ack!", ex);
        }
    }

    private ChatRequest deserializeChatRequest(Message message) throws ChatException {
        try {
            return objectMapper.readValue(message.getBody(), ChatRequest.class);
        } catch (Exception e) {
            throw new ChatException(ChatException.Code.CODE_BAD_REQUEST,
                    "Failed to deserialize chat request: " + e.getMessage());
        }
    }

    private String serializeChatResponse(ChatResponse response) throws ChatException {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Failed to serialize chat response", e);
            throw new ChatException(ChatException.Code.CODE_INTERNAL_SERVER_ERROR,
                    "Failed to serialize chat response: " + e.getMessage());
        }
    }

    private Optional<String> serializeChatError(Exception e) {
        Optional<ChatException> chatEx = e instanceof ChatException
                ? Optional.of((ChatException) e)
                : Optional.of(new ChatException(e.getMessage()));

        try {
            return chatEx
                    .map(ChatError::new)
                    .map(err -> {
                        try {
                            return objectMapper.writeValueAsString(err);
                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
            //return objectMapper.writeValueAsString(error).describeConstable();
        } catch (Exception ex) {
            logger.error("Failed to serialize error", ex);
            return Optional.empty();
        }
    }
}