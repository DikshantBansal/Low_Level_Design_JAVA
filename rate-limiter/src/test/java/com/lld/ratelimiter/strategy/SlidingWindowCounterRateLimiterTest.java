package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.SlidingWindowCounterConfig;
import com.lld.ratelimiter.model.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowCounterRateLimiterTest {
    @Test void weightsThePreviousWindowAsTimePasses() {
        MutableClock clock = new MutableClock();
        SlidingWindowCounterRateLimiter limiter = new SlidingWindowCounterRateLimiter(new SlidingWindowCounterConfig(4, Duration.ofSeconds(1)), clock);
        limiter.evaluate(new RateLimitRequest("client", 4));
        clock.advance(Duration.ofSeconds(1));
        RateLimitResult rejected = limiter.evaluate(new RateLimitRequest("client"));
        assertFalse(rejected.allowed()); assertEquals(Duration.ofMillis(250), rejected.retryAfter());
        clock.advance(Duration.ofMillis(250));
        assertTrue(limiter.evaluate(new RateLimitRequest("client")).allowed());
    }
}
