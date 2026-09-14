package com.lld.ratelimiter.model;

import java.time.Duration;
import java.util.Objects;

/** The immutable outcome of a rate-limit decision. */
public record RateLimitResult(boolean allowed, long remainingPermits, Duration retryAfter) {
    public RateLimitResult {
        if (remainingPermits < 0) throw new IllegalArgumentException("remainingPermits must not be negative");
        Objects.requireNonNull(retryAfter, "retryAfter must not be null");
        if (retryAfter.isNegative()) throw new IllegalArgumentException("retryAfter must not be negative");
        if (allowed && !retryAfter.isZero()) throw new IllegalArgumentException("allowed requests cannot have a retry delay");
    }

    public static RateLimitResult allowed(long remainingPermits) {
        return new RateLimitResult(true, remainingPermits, Duration.ZERO);
    }

    public static RateLimitResult rejected(long remainingPermits, Duration retryAfter) {
        return new RateLimitResult(false, remainingPermits, retryAfter);
    }
}
