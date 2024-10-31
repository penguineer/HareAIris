package com.penguineering.hareairis.ai;

import com.azure.core.exception.HttpResponseException;
import com.penguineering.hareairis.model.ChatException;
import com.penguineering.hareairis.model.ChatRequest;
import com.penguineering.hareairis.model.ChatResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
    @RateLimiter(name = "aiChatService")
    public ChatResponse handleChatRequest(ChatRequest chatRequest) {
        logger.info("Calling AI Service");
        try {
            AzureOpenAiChatOptions options = renderAzureOpenAiChatOptions(chatRequest);

            ChatClient chatClient = chatClientBuilder
                    .defaultOptions(options)
                    .defaultSystem(chatRequest.getSystemMessage())
                    .build();
            var chatResponse = chatClient
                    .prompt()
                    .user(chatRequest.getPrompt())
                    .call()
                    .chatResponse();

            String response = chatResponse.getResult().getOutput().getContent();

            Long promptTokens = chatResponse.getMetadata().getUsage().getPromptTokens();
            Long generationTokens = chatResponse.getMetadata().getUsage().getGenerationTokens();

            return ChatResponse.builder()
                    .response(response)
                    .inputTokens(promptTokens.intValue())
                    .outputTokens(generationTokens.intValue())
                    .build();
        } catch (IllegalArgumentException e) {
            throw new ChatException(ChatException.Code.CODE_BAD_REQUEST, e.getMessage());
        } catch (HttpResponseException e) {
            var response = e.getResponse();
            throw new ChatException(response.getStatusCode(), e.getMessage());
        }
    }

    private static AzureOpenAiChatOptions renderAzureOpenAiChatOptions(ChatRequest chatRequest) {
        AzureOpenAiChatOptions options = new AzureOpenAiChatOptions();

        if (Objects.nonNull(chatRequest.getMaxTokens()))
            options.setMaxTokens(chatRequest.getMaxTokens());
        if (Objects.nonNull(chatRequest.getTemperature()))
            options.setTemperature(chatRequest.getTemperature());
        if (Objects.nonNull(chatRequest.getTopP()))
            options.setTopP(chatRequest.getTopP());
        if (Objects.nonNull(chatRequest.getPresencePenalty()))
            options.setPresencePenalty(chatRequest.getPresencePenalty());
        if (Objects.nonNull(chatRequest.getFrequencyPenalty()))
            options.setFrequencyPenalty(chatRequest.getFrequencyPenalty());

        return options;
    }
}
