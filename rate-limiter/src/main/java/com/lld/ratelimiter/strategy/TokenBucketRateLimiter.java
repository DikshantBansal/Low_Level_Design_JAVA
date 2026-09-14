package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.TokenBucketConfig;
import com.lld.ratelimiter.model.RateLimitRequest;
import com.lld.ratelimiter.model.RateLimitResult;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Thread-safe in-memory token bucket with independent state per client. */
public final class TokenBucketRateLimiter implements RateLimiterStrategy {
    private final TokenBucketConfig config;
    private final Clock clock;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(TokenBucketConfig config) {
        this(config, Clock.systemUTC());
    }

    public TokenBucketRateLimiter(TokenBucketConfig config, Clock clock) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public RateLimitResult evaluate(RateLimitRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (request.permits() > config.capacity()) {
            throw new IllegalArgumentException("requested permits cannot exceed bucket capacity");
        }
        long nowMillis = clock.millis();
        Decision decision = new Decision();

        buckets.compute(request.clientId(), (ignored, current) -> {
            Bucket bucket = current == null ? new Bucket(config.capacity(), nowMillis) : refill(current, nowMillis);
            if (request.permits() <= bucket.tokens()) {
                double remaining = bucket.tokens() - request.permits();
                decision.result = RateLimitResult.allowed(wholeTokens(remaining));
                return new Bucket(remaining, nowMillis);
            }

            double deficit = request.permits() - bucket.tokens();
            long retryNanos = (long) Math.ceil(deficit / config.refillTokensPerSecond() * 1_000_000_000D);
            decision.result = RateLimitResult.rejected(wholeTokens(bucket.tokens()), Duration.ofNanos(Math.max(1, retryNanos)));
            return bucket;
        });
        return decision.result;
    }

    private Bucket refill(Bucket bucket, long nowMillis) {
        long elapsedMillis = Math.max(0, nowMillis - bucket.lastRefillMillis());
        if (elapsedMillis == 0) return bucket;
        double replenished = elapsedMillis / 1_000D * config.refillTokensPerSecond();
        return new Bucket(Math.min(config.capacity(), bucket.tokens() + replenished), nowMillis);
    }

    private static long wholeTokens(double tokens) {
        return (long) Math.floor(tokens + 1e-9);
    }

    private record Bucket(double tokens, long lastRefillMillis) {}

    private static final class Decision {
        private RateLimitResult result;
    }
}
