package com.lld.ratelimiter.service;

import com.lld.ratelimiter.model.RateLimitRequest;
import com.lld.ratelimiter.model.RateLimitResult;
import com.lld.ratelimiter.strategy.RateLimiterStrategy;

import java.util.Objects;

/** Application-facing facade that remains independent of the selected algorithm. */
public final class RateLimiterService {
    private final RateLimiterStrategy strategy;

    public RateLimiterService(RateLimiterStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
    }

    public RateLimitResult check(RateLimitRequest request) {
        return strategy.evaluate(request);
    }
}
