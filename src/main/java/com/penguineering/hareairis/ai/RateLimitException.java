package com.penguineering.hareairis.ai;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Exception thrown when the OpenAI service returns a rate-limiting error.
 */
public class RateLimitException extends RuntimeException {
    /**
     * Creates a RateLimitException from the specified HttpResponse.
     *
     * @param response The HttpResponse to create the exception from.
     * @param logConsumer The consumer to log any parsing errors.
     * @return The RateLimitException created from the HttpResponse.
     * @throws IllegalArgumentException If the response status code is not 429.
     */
    public static RateLimitException fromHttpResponse(HttpResponse response, Consumer<String> logConsumer) {
        if (response.getStatusCode() != 429)
            throw new IllegalArgumentException("Expected status code 429 for RateLimitException, but received " + response.getStatusCode() + ".");

        String errorMessage = response.getBodyAsString().block();
        String retryAfterHeader = response.getHeaders().getValue(HttpHeaderName.RETRY_AFTER);

        if (Objects.isNull(retryAfterHeader))
            return new RateLimitException(errorMessage);

        // Try parsing as Instant
        try {
            Instant retryAfter = Instant.parse(retryAfterHeader);
            return new RateLimitException(errorMessage, retryAfter);
        } catch (Exception ex) {
            // Ignore and try parsing as Duration
        }

        // Try parsing as seconds
        try {
            long seconds = Long.parseLong(retryAfterHeader);
            return new RateLimitException(errorMessage, Duration.ofSeconds(seconds));
        } catch (NumberFormatException e) {
            logConsumer.accept("Failed to parse Retry-After header and returning an exception with message only: " + retryAfterHeader);
        }

        return new RateLimitException(errorMessage);
    }

    private final Instant retryAfter;

    /**
     * Constructs a new RateLimitException with the specified detail message.
     *
     * @param message The detail message.
     */
    public RateLimitException(String message) {
        super(message);
        this.retryAfter = null;
    }

    /**
     * Constructs a new RateLimitException with the specified detail message and retry after time.
     *
     * @param message    The detail message.
     * @param retryAfter The optional point in time to hint when the service can be called again.
     */
    public RateLimitException(String message, Instant retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    /**
     * Constructs a new RateLimitException with the specified detail message and retry after duration.
     *
     * @param message  The detail message.
     * @param duration The duration after which the service can be called again.
     */
    public RateLimitException(String message, Duration duration) {
        super(message);
        this.retryAfter = Instant.now().plus(duration);
    }

    /**
     * Returns the optional point in time to hint when the service can be called again.
     *
     * @return The optional retry after time.
     */
    public Optional<Instant> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}