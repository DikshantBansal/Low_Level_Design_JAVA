package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.SlidingWindowCounterConfig;
import com.lld.ratelimiter.model.*;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Memory-efficient sliding window estimated from weighted current and previous counters. */
public final class SlidingWindowCounterRateLimiter implements RateLimiterStrategy {
    private final SlidingWindowCounterConfig config; private final Clock clock; private final long windowMillis;
    private final ConcurrentMap<String, Counters> counters = new ConcurrentHashMap<>();
    public SlidingWindowCounterRateLimiter(SlidingWindowCounterConfig config) { this(config, Clock.systemUTC()); }
    public SlidingWindowCounterRateLimiter(SlidingWindowCounterConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config); this.clock = Objects.requireNonNull(clock); this.windowMillis = config.window().toMillis();
    }

    @Override public RateLimitResult evaluate(RateLimitRequest request) {
        Objects.requireNonNull(request);
        if (request.permits() > config.permitLimit()) throw new IllegalArgumentException("requested permits cannot exceed window limit");
        long now = clock.millis(); long start = Math.floorDiv(now, windowMillis) * windowMillis; Decision decision = new Decision();
        counters.compute(request.clientId(), (key, existing) -> {
            Counters state = roll(existing, start);
            long elapsed = Math.max(0, now - start);
            double previousWeight = 1D - (double) elapsed / windowMillis;
            double estimated = state.current + state.previous * previousWeight;
            if (estimated + request.permits() <= config.permitLimit() + 1e-9) {
                state = new Counters(start, state.previous, state.current + request.permits());
                decision.result = RateLimitResult.allowed(whole(config.permitLimit() - estimated - request.permits()));
            } else {
                decision.result = RateLimitResult.rejected(whole(config.permitLimit() - estimated), retryAfter(state, elapsed, request.permits()));
            }
            return state;
        });
        return decision.result;
    }

    private Counters roll(Counters existing, long start) {
        if (existing == null) return new Counters(start, 0, 0);
        if (existing.start == start) return existing;
        if (start - existing.start == windowMillis) return new Counters(start, existing.current, 0);
        return new Counters(start, 0, 0);
    }

    private Duration retryAfter(Counters state, long elapsed, long permits) {
        double allowedPrevious = config.permitLimit() - state.current - permits;
        if (state.previous > 0 && allowedPrevious >= 0) {
            double targetElapsed = windowMillis * (1D - allowedPrevious / state.previous);
            return Duration.ofMillis(Math.max(1, (long) Math.ceil(targetElapsed - elapsed)));
        }
        long untilBoundary = windowMillis - elapsed;
        if (state.current == 0 || state.current + permits <= config.permitLimit()) return Duration.ofMillis(Math.max(1, untilBoundary));
        double decayInNext = windowMillis * (double) (state.current + permits - config.permitLimit()) / state.current;
        return Duration.ofMillis(Math.max(1, untilBoundary + (long) Math.ceil(decayInNext)));
    }

    private static long whole(double value) { return Math.max(0, (long) Math.floor(value + 1e-9)); }
    private record Counters(long start, long previous, long current) {}
    private static final class Decision { private RateLimitResult result; }
}
