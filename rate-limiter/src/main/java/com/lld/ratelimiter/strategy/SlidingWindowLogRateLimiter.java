package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.WindowRateLimitConfig;
import com.lld.ratelimiter.model.*;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Exact sliding window retaining accepted request timestamps and permit counts. */
public final class SlidingWindowLogRateLimiter implements RateLimiterStrategy {
    private final WindowRateLimitConfig config; private final Clock clock; private final long windowMillis;
    private final ConcurrentMap<String, ClientLog> logs = new ConcurrentHashMap<>();
    public SlidingWindowLogRateLimiter(WindowRateLimitConfig config) { this(config, Clock.systemUTC()); }
    public SlidingWindowLogRateLimiter(WindowRateLimitConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config); this.clock = Objects.requireNonNull(clock); this.windowMillis = config.window().toMillis();
    }

    @Override public RateLimitResult evaluate(RateLimitRequest request) {
        Objects.requireNonNull(request);
        if (request.permits() > config.permitLimit()) throw new IllegalArgumentException("requested permits cannot exceed window limit");
        long now = clock.millis(); Decision decision = new Decision();
        logs.compute(request.clientId(), (key, existing) -> {
            ClientLog log = existing == null ? new ClientLog() : existing;
            while (!log.entries.isEmpty() && log.entries.peekFirst().timestamp <= now - windowMillis) {
                log.used -= log.entries.removeFirst().permits;
            }
            if (request.permits() <= config.permitLimit() - log.used) {
                log.entries.addLast(new Entry(now, request.permits())); log.used += request.permits();
                decision.result = RateLimitResult.allowed(config.permitLimit() - log.used);
            } else {
                long needed = request.permits() - (config.permitLimit() - log.used); long released = 0; long retryAt = now;
                for (Entry entry : log.entries) { released += entry.permits; retryAt = entry.timestamp + windowMillis; if (released >= needed) break; }
                decision.result = RateLimitResult.rejected(config.permitLimit() - log.used, Duration.ofMillis(Math.max(1, retryAt - now)));
            }
            return log;
        });
        return decision.result;
    }

    private record Entry(long timestamp, long permits) {}
    private static final class ClientLog { private final Deque<Entry> entries = new ArrayDeque<>(); private long used; }
    private static final class Decision { private RateLimitResult result; }
}
