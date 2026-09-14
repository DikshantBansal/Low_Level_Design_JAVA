package com.lld.ratelimiter.service;

import com.lld.ratelimiter.model.RateLimitRequest;
import com.lld.ratelimiter.model.RateLimitResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class RateLimiterServiceTest {
    @Test
    void delegatesToConfiguredStrategy() {
        RateLimitResult expected = RateLimitResult.allowed(4);
        RateLimiterService service = new RateLimiterService(request -> expected);
        assertSame(expected, service.check(new RateLimitRequest("client-a")));
    }
}
