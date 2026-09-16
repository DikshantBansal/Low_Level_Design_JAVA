package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.LeakyBucketConfig;
import com.lld.ratelimiter.model.RateLimitRequest;
import com.lld.ratelimiter.model.RateLimitResult;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Leaky bucket that models queued work draining at a constant rate. */
public final class LeakyBucketRateLimiter implements RateLimiterStrategy {
    private final LeakyBucketConfig config;
    private final Clock clock;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public LeakyBucketRateLimiter(LeakyBucketConfig config) { this(config, Clock.systemUTC()); }
    public LeakyBucketRateLimiter(LeakyBucketConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config); this.clock = Objects.requireNonNull(clock);
    }

    @Override public RateLimitResult evaluate(RateLimitRequest request) {
        Objects.requireNonNull(request);
        if (request.permits() > config.capacity()) throw new IllegalArgumentException("requested permits cannot exceed bucket capacity");
        long now = clock.millis(); Decision decision = new Decision();
        buckets.compute(request.clientId(), (key, current) -> {
            Bucket bucket = current == null ? new Bucket(0, now) : leak(current, now);
            if (bucket.level + request.permits() <= config.capacity()) {
                double level = bucket.level + request.permits();
                decision.result = RateLimitResult.allowed(whole(config.capacity() - level));
                return new Bucket(level, now);
            }
            double excess = bucket.level + request.permits() - config.capacity();
            long nanos = (long) Math.ceil(excess / config.leakPermitsPerSecond() * 1_000_000_000D);
            decision.result = RateLimitResult.rejected(whole(config.capacity() - bucket.level), Duration.ofNanos(Math.max(1, nanos)));
            return bucket;
        });
        return decision.result;
    }

    private Bucket leak(Bucket bucket, long now) {
        long elapsed = Math.max(0, now - bucket.lastLeakMillis);
        double level = Math.max(0, bucket.level - elapsed / 1_000D * config.leakPermitsPerSecond());
        return elapsed == 0 ? bucket : new Bucket(level, now);
    }
    private static long whole(double value) { return (long) Math.floor(value + 1e-9); }
    private record Bucket(double level, long lastLeakMillis) {}
    private static final class Decision { private RateLimitResult result; }
}
