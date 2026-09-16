package com.lld.ratelimiter.strategy;

import com.lld.ratelimiter.config.*;
import com.lld.ratelimiter.model.RateLimitRequest;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimiterConcurrencyTest {
    @Test void strategiesDoNotOversubscribeOneClient() throws Exception {
        int capacity = 20;
        List<Function<MutableClock, RateLimiterStrategy>> factories = List.of(
                clock -> new LeakyBucketRateLimiter(new LeakyBucketConfig(capacity, 1), clock),
                clock -> new FixedWindowRateLimiter(new WindowRateLimitConfig(capacity, Duration.ofMinutes(1)), clock),
                clock -> new SlidingWindowLogRateLimiter(new WindowRateLimitConfig(capacity, Duration.ofMinutes(1)), clock),
                clock -> new SlidingWindowCounterRateLimiter(new SlidingWindowCounterConfig(capacity, Duration.ofMinutes(1)), clock));

        for (Function<MutableClock, RateLimiterStrategy> factory : factories) {
            RateLimiterStrategy limiter = factory.apply(new MutableClock());
            ExecutorService pool = Executors.newFixedThreadPool(12); CountDownLatch start = new CountDownLatch(1);
            try {
                List<Future<Boolean>> results = new java.util.ArrayList<>();
                for (int i = 0; i < 100; i++) results.add(pool.submit(() -> { start.await(); return limiter.evaluate(new RateLimitRequest("shared")).allowed(); }));
                start.countDown(); long allowed = 0;
                for (Future<Boolean> result : results) if (result.get(5, TimeUnit.SECONDS)) allowed++;
                assertEquals(capacity, allowed, limiter.getClass().getSimpleName());
            } finally { pool.shutdownNow(); }
        }
    }
}
