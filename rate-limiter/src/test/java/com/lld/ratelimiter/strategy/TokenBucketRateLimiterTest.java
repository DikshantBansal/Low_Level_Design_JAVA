package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.TokenBucketConfig;
import com.lld.ratelimiter.model.RateLimitRequest;
import com.lld.ratelimiter.model.RateLimitResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {
    private final MutableClock clock = new MutableClock();

    @Test
    void allowsUpToCapacityThenRejects() {
        TokenBucketRateLimiter limiter = limiter(3, 1);
        assertTrue(limiter.evaluate(new RateLimitRequest("client-a", 2)).allowed());
        assertTrue(limiter.evaluate(new RateLimitRequest("client-a")).allowed());

        RateLimitResult rejected = limiter.evaluate(new RateLimitRequest("client-a"));
        assertFalse(rejected.allowed());
        assertEquals(0, rejected.remainingPermits());
        assertEquals(Duration.ofSeconds(1), rejected.retryAfter());
    }

    @Test
    void refillsOverTimeWithoutExceedingCapacity() {
        TokenBucketRateLimiter limiter = limiter(5, 2);
        limiter.evaluate(new RateLimitRequest("client-a", 5));
        clock.advance(Duration.ofMillis(1_500));

        RateLimitResult result = limiter.evaluate(new RateLimitRequest("client-a", 2));
        assertTrue(result.allowed());
        assertEquals(1, result.remainingPermits());

        clock.advance(Duration.ofMinutes(10));
        assertEquals(4, limiter.evaluate(new RateLimitRequest("client-a")).remainingPermits());
    }

    @Test
    void isolatesClients() {
        TokenBucketRateLimiter limiter = limiter(1, 1);
        assertTrue(limiter.evaluate(new RateLimitRequest("client-a")).allowed());
        assertFalse(limiter.evaluate(new RateLimitRequest("client-a")).allowed());
        assertTrue(limiter.evaluate(new RateLimitRequest("client-b")).allowed());
    }

    @Test
    void rejectsRequestsThatCanNeverFitInTheBucket() {
        TokenBucketRateLimiter limiter = limiter(2, 1);
        assertThrows(IllegalArgumentException.class,
                () -> limiter.evaluate(new RateLimitRequest("client-a", 3)));
    }

    @Test
    void computesRetryForPartialTokens() {
        TokenBucketRateLimiter limiter = limiter(2, 2);
        limiter.evaluate(new RateLimitRequest("client-a", 2));
        clock.advance(Duration.ofMillis(250));
        assertEquals(Duration.ofMillis(250), limiter.evaluate(new RateLimitRequest("client-a")).retryAfter());
    }

    @Test
    void neverAllowsMoreConcurrentRequestsThanCapacity() throws Exception {
        int capacity = 25;
        TokenBucketRateLimiter limiter = limiter(capacity, 1);
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 200; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return limiter.evaluate(new RateLimitRequest("shared-client")).allowed();
                }));
            }
            start.countDown();
            long allowed = 0;
            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) allowed++;
            }
            assertEquals(capacity, allowed);
        } finally {
            executor.shutdownNow();
        }
    }

    private TokenBucketRateLimiter limiter(long capacity, double refillRate) {
        return new TokenBucketRateLimiter(new TokenBucketConfig(capacity, refillRate), clock);
    }

    private static final class MutableClock extends Clock {
        private final AtomicLong millis = new AtomicLong();

        void advance(Duration duration) { millis.addAndGet(duration.toMillis()); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis.get()); }
        @Override public long millis() { return millis.get(); }
    }
}
