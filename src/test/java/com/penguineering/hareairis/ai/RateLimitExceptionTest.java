package com.penguineering.hareairis.ai;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Rate limit exceeded";
        RateLimitException exception = new RateLimitException(message);

        assertEquals(message, exception.getMessage(), "The exception message should match the provided message.");
        assertTrue(exception.getRetryAfter().isEmpty(), "The retryAfter should be empty when not provided.");
    }

    @Test
    void testConstructorWithMessageAndInstant() {
        String message = "Rate limit exceeded";
        Instant retryAfter = Instant.now().plus(Duration.ofMinutes(1));
        RateLimitException exception = new RateLimitException(message, retryAfter);

        assertEquals(message, exception.getMessage(), "The exception message should match the provided message.");
        assertTrue(exception.getRetryAfter().isPresent(), "The retryAfter should be present when provided.");
        assertEquals(retryAfter, exception.getRetryAfter().get(), "The retryAfter should match the provided Instant.");
    }

    @Test
    void testConstructorWithMessageAndDuration() {
        String message = "Rate limit exceeded";
        Duration duration = Duration.ofMinutes(1);
        Instant beforeCreation = Instant.now();
        RateLimitException exception = new RateLimitException(message, duration);
        Instant afterCreation = Instant.now();

        assertEquals(message, exception.getMessage(), "The exception message should match the provided message.");
        assertTrue(exception.getRetryAfter().isPresent(), "The retryAfter should be present when duration is provided.");
        Instant retryAfter = exception.getRetryAfter().get();
        assertTrue(retryAfter.isAfter(beforeCreation), "The retryAfter should be after the time before creation.");
        assertTrue(retryAfter.isBefore(afterCreation.plus(duration)), "The retryAfter should be within the expected duration range.");
    }
}