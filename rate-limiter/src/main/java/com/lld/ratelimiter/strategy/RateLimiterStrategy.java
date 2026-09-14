package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.model.RateLimitRequest;
import com.lld.ratelimiter.model.RateLimitResult;

/** Common algorithm boundary for making rate-limit decisions. */
@FunctionalInterface
public interface RateLimiterStrategy {
    RateLimitResult evaluate(RateLimitRequest request);
}
