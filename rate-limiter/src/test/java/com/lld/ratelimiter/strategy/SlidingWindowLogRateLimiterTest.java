package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.WindowRateLimitConfig;
import com.lld.ratelimiter.model.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowLogRateLimiterTest {
    @Test void expiresOnlyRequestsOutsideTheRollingWindow() {
        MutableClock clock = new MutableClock();
        SlidingWindowLogRateLimiter limiter = new SlidingWindowLogRateLimiter(new WindowRateLimitConfig(3, Duration.ofSeconds(1)), clock);
        limiter.evaluate(new RateLimitRequest("client", 2));
        clock.advance(Duration.ofMillis(400));
        limiter.evaluate(new RateLimitRequest("client"));
        RateLimitResult rejected = limiter.evaluate(new RateLimitRequest("client"));
        assertFalse(rejected.allowed()); assertEquals(Duration.ofMillis(600), rejected.retryAfter());
        clock.advance(Duration.ofMillis(600));
        RateLimitResult allowed = limiter.evaluate(new RateLimitRequest("client"));
        assertTrue(allowed.allowed()); assertEquals(1, allowed.remainingPermits());
    }
}
