package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @JsonProperty("request_id")
    private UUID requestId;

    @JsonProperty("message")
    private String message;
}