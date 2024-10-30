package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatException extends RuntimeException {
    @Getter
    @AllArgsConstructor
    public enum Code {
        CODE_BAD_REQUEST(400),
        CODE_TOO_MANY_REQUESTS(429),
        CODE_INTERNAL_SERVER_ERROR(500),
        CODE_GATEWAY_TIMEOUT(504);

        private final int code;
    }

    @Getter
    private final int code;

    public ChatException(String message) {
        super(message);
        this.code = Code.CODE_INTERNAL_SERVER_ERROR.getCode();
    }

    public ChatException(Code code, String message) {
        super(message);
        this.code = code.getCode();
    }

    public ChatException(int code, String message) {
        super(message);
        this.code = code;
    }

    public boolean is5xxServerError() {
        return code >= 500 && code < 600;
    }
}