package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.LeakyBucketConfig;
import com.lld.ratelimiter.model.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class LeakyBucketRateLimiterTest {
    private final MutableClock clock = new MutableClock();

    @Test void drainsAtConfiguredRate() {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(new LeakyBucketConfig(3, 2), clock);
        assertTrue(limiter.evaluate(new RateLimitRequest("client", 3)).allowed());
        RateLimitResult rejected = limiter.evaluate(new RateLimitRequest("client"));
        assertFalse(rejected.allowed()); assertEquals(Duration.ofMillis(500), rejected.retryAfter());
        clock.advance(Duration.ofMillis(500));
        assertTrue(limiter.evaluate(new RateLimitRequest("client")).allowed());
    }

    @Test void keepsClientBucketsIndependent() {
        LeakyBucketRateLimiter limiter = new LeakyBucketRateLimiter(new LeakyBucketConfig(1, 1), clock);
        assertTrue(limiter.evaluate(new RateLimitRequest("a")).allowed());
        assertFalse(limiter.evaluate(new RateLimitRequest("a")).allowed());
        assertTrue(limiter.evaluate(new RateLimitRequest("b")).allowed());
    }
}
