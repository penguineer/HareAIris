package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResponse {
    @JsonProperty("response")
    private String response;

    @JsonProperty("input-tokens")
    private int inputTokens;

    @JsonProperty("output-tokens")
    private int outputTokens;
}