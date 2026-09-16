package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.WindowRateLimitConfig;
import com.lld.ratelimiter.model.*;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Counter reset at epoch-aligned fixed-window boundaries. */
public final class FixedWindowRateLimiter implements RateLimiterStrategy {
    private final WindowRateLimitConfig config; private final Clock clock; private final long windowMillis;
    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();
    public FixedWindowRateLimiter(WindowRateLimitConfig config) { this(config, Clock.systemUTC()); }
    public FixedWindowRateLimiter(WindowRateLimitConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config); this.clock = Objects.requireNonNull(clock); this.windowMillis = config.window().toMillis();
    }

    @Override public RateLimitResult evaluate(RateLimitRequest request) {
        Objects.requireNonNull(request); requireFits(request);
        long now = clock.millis(); long start = Math.floorDiv(now, windowMillis) * windowMillis; Decision decision = new Decision();
        windows.compute(request.clientId(), (key, current) -> {
            Window window = current == null || current.start != start ? new Window(start, 0) : current;
            if (request.permits() <= config.permitLimit() - window.used) {
                long used = window.used + request.permits(); decision.result = RateLimitResult.allowed(config.permitLimit() - used); return new Window(start, used);
            }
            decision.result = RateLimitResult.rejected(config.permitLimit() - window.used, Duration.ofMillis(start + windowMillis - now)); return window;
        });
        return decision.result;
    }
    private void requireFits(RateLimitRequest request) { if (request.permits() > config.permitLimit()) throw new IllegalArgumentException("requested permits cannot exceed window limit"); }
    private record Window(long start, long used) {}
    private static final class Decision { private RateLimitResult result; }
}
