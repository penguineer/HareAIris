package com.penguineering.hareairis.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRequest {
    @JsonProperty("system-message")
    private String systemMessage = "";

    @JsonProperty("prompt")
    private String prompt = "";

    /**
     * The maximum number of tokens to generate.
     */
    @JsonProperty(value = "max-tokens")
    private Integer maxTokens = null;

    /**
     * The sampling temperature to use that controls the apparent creativity of generated
     * completions. Higher values will make output more random while lower values will
     * make results more focused and deterministic. It is not recommended to modify
     * temperature and top_p for the same completions request as the interaction of these
     * two settings is difficult to predict.
     */
    @JsonProperty(value = "temperature")
    private Double temperature = null;

    /**
     * An alternative to sampling with temperature called nucleus sampling. This value
     * causes the model to consider the results of tokens with the provided probability
     * mass. As an example, a value of 0.15 will cause only the tokens comprising the top
     * 15% of probability mass to be considered. It is not recommended to modify
     * temperature and top_p for the same completions request as the interaction of these
     * two settings is difficult to predict.
     */
    @JsonProperty(value = "top-p")
    private Double topP = null;

    /**
     * A value that influences the probability of generated tokens appearing based on
     * their existing presence in generated text. Positive values will make tokens less
     * likely to appear when they already exist and increase the model's likelihood to
     * output new topics.
     */
    @JsonProperty(value = "presence-penalty")
    private Double presencePenalty = null;

    /**
     * A value that influences the probability of generated tokens appearing based on
     * their cumulative frequency in generated text. Positive values will make tokens less
     * likely to appear as their frequency increases and decrease the likelihood of the
     * model repeating the same statements verbatim.
     */
    @JsonProperty(value = "frequency-penalty")
    private Double frequencyPenalty = null;
}