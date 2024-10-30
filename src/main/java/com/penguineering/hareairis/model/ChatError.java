package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatError extends RuntimeException {
    @Getter
    @AllArgsConstructor
    public enum Code {
        CODE_BAD_REQUEST(400),
        CODE_TOO_MANY_REQUESTS(429),
        CODE_INTERNAL_SERVER_ERROR(500),
        CODE_GATEWAY_TIMEOUT(504);

        private final int code;
    }

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    public ChatError(String message) {
        super(message);
        this.code = Code.CODE_INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
    }

    public ChatError(Code code, String message) {
        super(message);
        this.code = code.getCode();
        this.message = message;
    }
}