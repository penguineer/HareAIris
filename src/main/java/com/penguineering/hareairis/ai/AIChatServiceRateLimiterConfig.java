package com.penguineering.hareairis.ai;

import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

//@Configuration
public class AIChatServiceRateLimiterConfig {

    @Value("${OPENAI_RATE_LIMIT:50}")
    private int limitForPeriod;

    @Value("${OPENAI_RATE_PERIOD_S:60}")
    private int refreshPeriod;


    //  @Bean
    public RateLimiterConfigCustomizer rateLimiterConfigCustomizer() {
        return RateLimiterConfigCustomizer.of("aiChatService", builder ->
                builder.limitForPeriod(limitForPeriod)
                        .limitRefreshPeriod(Duration.ofSeconds(refreshPeriod))
                        .timeoutDuration(Duration.ZERO)
        );
    }
}