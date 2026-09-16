package com.lld.ratelimiter.config;

import java.time.Duration;
import java.util.Objects;

/** Configuration for the memory-efficient weighted sliding-window counter. */
public record SlidingWindowCounterConfig(long permitLimit, Duration window) {
    public SlidingWindowCounterConfig {
        if (permitLimit <= 0) throw new IllegalArgumentException("permitLimit must be positive");
        Objects.requireNonNull(window, "window must not be null");
        if (window.isZero() || window.isNegative()) throw new IllegalArgumentException("window must be positive");
        try {
            if (window.toMillis() <= 0) throw new IllegalArgumentException("window must be at least one millisecond");
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException("window is too large", error);
        }
    }
}
