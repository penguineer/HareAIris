package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a chat error.
 *
 * <p>Represents a chat error that can be sent back to the chat client.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatError {
    /**
     * The error code, matching an HTTP status code.
     */
    @JsonProperty("code")
    private int code;

    /**
     * The error message.
     */
    @JsonProperty("message")
    private String message;

    /**
     * Creates a new chat error from a chat exception.
     *
     * @param ex The chat exception.
     */
    public ChatError(ChatException ex) {
        this.code = ex.getCode();
        this.message = ex.getMessage();
    }
}
