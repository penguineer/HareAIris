package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatError {
    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    public ChatError(ChatException ex) {
        this.code = ex.getCode();
        this.message = ex.getMessage();
    }
}
