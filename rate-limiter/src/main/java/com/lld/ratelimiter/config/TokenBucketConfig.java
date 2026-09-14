package com.lld.ratelimiter.config;

/** Immutable token-bucket settings shared by all clients of one limiter instance. */
public record TokenBucketConfig(long capacity, double refillTokensPerSecond) {
    public TokenBucketConfig {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        if (!Double.isFinite(refillTokensPerSecond) || refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException("refillTokensPerSecond must be finite and positive");
        }
    }
}
