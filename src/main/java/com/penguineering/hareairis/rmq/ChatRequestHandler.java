package com.penguineering.hareairis.rmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penguineering.hareairis.model.ChatRequest;
import com.penguineering.hareairis.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatRequestHandler.class);
    private final ObjectMapper objectMapper;
    private final ChatClient.Builder chatClientBuilder;
    private final RabbitTemplate rabbitTemplate;

    public ChatRequestHandler(ObjectMapper objectMapper,
                              ChatClient.Builder builder,
                              RabbitTemplate rabbitTemplate) {
        this.objectMapper = objectMapper;
        this.chatClientBuilder = builder;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void handleMessage(Message message) {
        try {
            logger.info("Received message: {}", new String(message.getBody()));
            ChatRequest chatRequest = objectMapper.readValue(message.getBody(), ChatRequest.class);

            // Extract the "reply-to" header
            MessageProperties properties = message.getMessageProperties();
            String replyTo = properties.getReplyTo();
            logger.info("Reply-to header: {}", replyTo);

            ChatClient chatClient = chatClientBuilder.build();
            String response = chatClient
                    .prompt(chatRequest.getMessage())
                    .call()
                    .content();

            logger.info("Received response from OpenAI: {}", response);

            ChatResponse chatResponse = new ChatResponse(chatRequest.getRequestId(), response);

            // Convert ChatResponse to JSON
            String jsonResponse = objectMapper.writeValueAsString(chatResponse);

            // Send the response to the replyTo queue
            rabbitTemplate.convertAndSend(replyTo, jsonResponse);
        } catch (Exception e) {
            logger.error("Failed to process message", e);
        }
    }
}