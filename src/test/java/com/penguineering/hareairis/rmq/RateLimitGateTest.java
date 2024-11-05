package com.penguineering.hareairis.rmq;

import com.penguineering.hareairis.ai.RateLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitGateTest {
    private RateLimitGate rateLimitGate;

    @Mock
    private Callable<String> protectedCall;

    @BeforeEach
    void setUp() {
        rateLimitGate = new RateLimitGate();
    }

    @Test
    void testCallWithRateLimit_Success() throws Exception {
        when(protectedCall.call()).thenReturn("Success");

        String result = rateLimitGate.callWithRateLimit(protectedCall);

        assertEquals("Success", result);
        verify(protectedCall, times(1)).call();
    }

    @Test
    void testCallWithRateLimit_RateLimitException() throws Exception {
        RateLimitException rateLimitException = new RateLimitException("Rate limit exceeded", Duration.ofSeconds(1));
        when(protectedCall.call()).thenThrow(rateLimitException).thenReturn("Success");

        String result = rateLimitGate.callWithRateLimit(protectedCall);

        assertEquals("Success", result);
        verify(protectedCall, times(2)).call();
    }

    @Test
    void testCallWithRateLimit_InterruptedException() throws Exception {
        when(protectedCall.call()).thenThrow(new InterruptedException("Interrupted"));

        assertThrows(InterruptedException.class, () -> rateLimitGate.callWithRateLimit(protectedCall));
        verify(protectedCall, times(1)).call();
    }

    @Test
    void testRegisterRateLimitException() {
        RateLimitException rateLimitException = new RateLimitException("Rate limit exceeded", Duration.ofSeconds(1));
        rateLimitGate.registerRateLimitException(rateLimitException);

        assertTrue(rateLimitGate.nextAvailableTime.get().isAfter(Instant.now()));
    }

    @Test
    void testInterruptWaitingThread() {
        Thread mockThread = mock(Thread.class);
        rateLimitGate.waitingThread.set(mockThread);

        rateLimitGate.interruptWaitingThread();

        verify(mockThread, times(1)).interrupt();
    }
}