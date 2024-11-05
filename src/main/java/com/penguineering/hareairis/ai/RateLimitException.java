package com.penguineering.hareairis.ai;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Exception thrown when the OpenAI service returns a rate-limiting error.
 */
public class RateLimitException extends RuntimeException {
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