package com.penguineering.hareairis.ai;

import com.penguineering.hareairis.model.ChatRequest;
import com.penguineering.hareairis.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Service to handle chat requests.
 *
 * <p>Uses the OpenAI ChatClient to handle chat requests.</p>
 */
@Service
public class AIChatService {
    private static final Logger logger = LoggerFactory.getLogger(AIChatService.class);
    private final ChatClient.Builder chatClientBuilder;

    public AIChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    /**
     * Handles a chat request.
     *
     * @param chatRequest The chat request to handle.
     * @return The chat response.
     */
    public ChatResponse handleChatRequest(ChatRequest chatRequest) {
        ChatClient chatClient = chatClientBuilder.build();
        var chatResponse = chatClient
                .prompt()
                .user(chatRequest.getMessage())
                .call()
                .chatResponse();

        String response = chatResponse.getResult().getOutput().getContent();

        logger.info("Received response from OpenAI: {}", response);

        Long promptTokens = chatResponse.getMetadata().getUsage().getPromptTokens();
        Long generationTokens = chatResponse.getMetadata().getUsage().getGenerationTokens();

        return ChatResponse.builder()
                .response(response)
                .inputTokens(promptTokens.intValue())
                .outputTokens(generationTokens.intValue())
                .build();
    }
}
