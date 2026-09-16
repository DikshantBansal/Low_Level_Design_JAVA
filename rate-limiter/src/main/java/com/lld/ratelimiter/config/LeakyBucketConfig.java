package com.lld.ratelimiter.config;

/** Maximum queued work and the continuous rate at which it drains. */
public record LeakyBucketConfig(long capacity, double leakPermitsPerSecond) {
    public LeakyBucketConfig {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be positive");
        if (!Double.isFinite(leakPermitsPerSecond) || leakPermitsPerSecond <= 0) {
            throw new IllegalArgumentException("leakPermitsPerSecond must be finite and positive");
        }
    }
}
