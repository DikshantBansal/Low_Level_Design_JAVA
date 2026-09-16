package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.WindowRateLimitConfig;
import com.lld.ratelimiter.model.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class FixedWindowRateLimiterTest {
    @Test void resetsAtTheAlignedWindowBoundary() {
        MutableClock clock = new MutableClock();
        FixedWindowRateLimiter limiter = new FixedWindowRateLimiter(new WindowRateLimitConfig(2, Duration.ofSeconds(1)), clock);
        clock.advance(Duration.ofMillis(999));
        assertTrue(limiter.evaluate(new RateLimitRequest("client", 2)).allowed());
        RateLimitResult rejected = limiter.evaluate(new RateLimitRequest("client"));
        assertEquals(Duration.ofMillis(1), rejected.retryAfter());
        clock.advance(Duration.ofMillis(1));
        assertTrue(limiter.evaluate(new RateLimitRequest("client", 2)).allowed());
    }
}
