package com.penguineering.hareairis.rmq;

import com.penguineering.hareairis.ai.RateLimitException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Rate limit gate to protect against rate limiting.
 *
 * <p>Protects against rate limiting by waiting for the next available time before executing a protected call.</p>
 */
@Component
public class RateLimitGate {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitGate.class);

    private final AtomicReference<Instant> nextAvailableTime = new AtomicReference<>(Instant.now());
    private final AtomicReference<Thread> waitingThread = new AtomicReference<>(null);
    private final Lock threadLock = new ReentrantLock();
    private final Condition threadCondition = threadLock.newCondition();

    /**
     * Calls the protected call with rate limiting.
     *
     * @param protectedCall The protected call to execute.
     * @param <T>           The type of the result.
     * @return The result of the protected call.
     * @throws Exception            If the protected call throws an exception.
     * @throws RateLimitException   If the protected call throws a rate limit exception.
     * @throws InterruptedException If the waiting thread is interrupted.
     */
    public <T> T callWithRateLimit(Callable<T> protectedCall) throws Exception {
        T result = null;

        do
            try {
                result = waitAndExecute(protectedCall);
            } catch (RateLimitException e) {
                if (e.getRetryAfter().isEmpty())
                    throw e;

                registerRateLimitException(e);
            }
        while (Objects.isNull(result));

        return result;
    }

    /**
     * Waits for the next available time and executes the protected call.
     *
     * @param protectedCall The protected call to execute.
     * @param <T>           The type of the result.
     * @return The result of the protected call.
     * @throws Exception            If the protected call throws an exception.
     * @throws InterruptedException If the waiting thread is interrupted.
     */
    public <T> T waitAndExecute(Callable<T> protectedCall) throws Exception {
        threadLock.lock();
        try {
            while (!waitingThread.compareAndSet(null, Thread.currentThread())) {
                threadCondition.await();
            }

            waitingThread.set(Thread.currentThread());

            do {
                Duration waitTime = Duration.between(Instant.now(), nextAvailableTime.get());
                if (waitTime.isNegative())
                    break;

                logger.warn("Rate limit exceeded, waiting for {} seconds...", waitTime.getSeconds());
                Thread.sleep(waitTime);
            } while (Instant.now().isBefore(nextAvailableTime.get()));

            return protectedCall.call();
        } finally {
            waitingThread.set(null);
            threadCondition.signalAll();
            threadLock.unlock();
        }
    }

    /**
     * Registers a rate-limit exception.
     *
     * @param e The rate-limit exception to register.
     */
    public void registerRateLimitException(RateLimitException e) {
        e.getRetryAfter().ifPresentOrElse(
                retryAfter -> nextAvailableTime.updateAndGet(
                        current -> {
                            Instant newTargetTime = retryAfter.isAfter(current) ? retryAfter : current;
                            logger.info("Rate limit registered, next available time set to {}", newTargetTime);
                            return newTargetTime;
                        }),
                () -> logger.warn("Tried to register a rate limit exception without a retry-after time, ignored."));
    }

    /**
     * Interrupts the waiting thread if it is active.
     */
    @PreDestroy
    public void interruptWaitingThread() {
        Optional.of(waitingThread)
                .map(AtomicReference::get)
                .ifPresent(thread -> {
                    logger.warn("Waiting thread is active, but application is shutting down. Interrupting...");
                    thread.interrupt();
                });
    }
}