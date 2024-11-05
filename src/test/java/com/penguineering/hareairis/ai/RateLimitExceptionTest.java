package com.penguineering.hareairis.ai;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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


    @Test
    void testFromHttpResponseWithoutRetryAfterHeader() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(429);
        when(response.getBodyAsString()).thenReturn(Mono.just("Rate limit exceeded"));
        when(response.getHeaders()).thenReturn(new HttpHeaders());

        RateLimitException exception = RateLimitException.fromHttpResponse(response, System.out::println);

        assertEquals("Rate limit exceeded", exception.getMessage());
        assertTrue(exception.getRetryAfter().isEmpty());
    }

    // Apply similar changes to other test methods
    @Test
    void testFromHttpResponseWithRetryAfterDuration() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(429);
        when(response.getBodyAsString()).thenReturn(Mono.just("Rate limit exceeded"));
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "60");
        when(response.getHeaders()).thenReturn(headers);

        RateLimitException exception = RateLimitException.fromHttpResponse(response, System.out::println);

        assertEquals("Rate limit exceeded", exception.getMessage());
        assertTrue(exception.getRetryAfter().isPresent());
        assertTrue(exception.getRetryAfter().get().isAfter(Instant.now().minusSeconds(60)));
    }

    @Test
    void testFromHttpResponseWithRetryAfterInstant() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(429);
        when(response.getBodyAsString()).thenReturn(Mono.just("Rate limit exceeded"));
        Instant retryAfter = Instant.now().plus(Duration.ofMinutes(1));
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, retryAfter.toString());
        when(response.getHeaders()).thenReturn(headers);

        RateLimitException exception = RateLimitException.fromHttpResponse(response, System.out::println);

        assertEquals("Rate limit exceeded", exception.getMessage());
        assertTrue(exception.getRetryAfter().isPresent());
        assertEquals(retryAfter, exception.getRetryAfter().get());
    }

    @Mock
    Consumer<String> logConsumer;

    @Test
    void testFromHttpResponseWithInvalidRetryAfterHeader() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(429);
        when(response.getBodyAsString()).thenReturn(Mono.just("Rate limit exceeded"));
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.RETRY_AFTER, "invalid");
        when(response.getHeaders()).thenReturn(headers);

        RateLimitException exception = RateLimitException.fromHttpResponse(response, logConsumer);

        assertEquals("Rate limit exceeded", exception.getMessage());
        assertTrue(exception.getRetryAfter().isEmpty());
        verify(logConsumer).accept("Failed to parse Retry-After header and returning an exception with message only: invalid");
    }

    @Test
    void testFromHttpResponseWithNon429StatusCode() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(400); // Any status code other than 429

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            throw RateLimitException.fromHttpResponse(response, logConsumer);
        });

        assertEquals("Expected status code 429 for RateLimitException, but received " + response.getStatusCode() + ".", exception.getMessage());
    }
}