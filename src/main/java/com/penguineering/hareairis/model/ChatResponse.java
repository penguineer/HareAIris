package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Represents a chat response.
 *
 * <p>Represents a chat response that can be sent back to the chat client.</p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {
    /**
     * The response from the AI service.
     */
    @JsonProperty("response")
    private String response;

    /**
     * The number of input tokens.
     */
    @JsonProperty("input-tokens")
    private int inputTokens;

    /**
     * The number of output tokens.
     */
    @JsonProperty("output-tokens")
    private int outputTokens;
}
