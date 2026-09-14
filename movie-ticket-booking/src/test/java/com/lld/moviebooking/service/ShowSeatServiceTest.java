package com.lld.moviebooking.service;

import com.lld.moviebooking.model.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class ShowSeatServiceTest {
    @Test void locksAndConfirmsSeatsAtomically() {
        ShowSeatService service = service(Clock.systemUTC());
        SeatLock lock = service.lock("u1", Set.of("A1", "A2"));
        assertEquals(SeatStatus.LOCKED, service.status("A1"));
        service.confirm(lock.token(), "u1");
        assertEquals(SeatStatus.BOOKED, service.status("A1"));
        assertThrows(IllegalStateException.class, () -> service.lock("u2", Set.of("A1")));
    }

    @Test void onlyOneConcurrentUserCanLockASeat() throws Exception {
        ShowSeatService service = service(Clock.systemUTC());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Callable<Boolean> attempt = () -> { start.await(); try { service.lock(Thread.currentThread().getName(), Set.of("A1")); return true; } catch (IllegalStateException e) { return false; } };
            Future<Boolean> first = pool.submit(attempt); Future<Boolean> second = pool.submit(attempt); start.countDown();
            assertEquals(1, (first.get() ? 1 : 0) + (second.get() ? 1 : 0));
        } finally { pool.shutdownNow(); }
    }

    @Test void expiredLockMakesSeatAvailable() {
        MutableClock clock = new MutableClock();
        ShowSeatService service = service(clock);
        service.lock("u1", Set.of("A1"));
        clock.advance(Duration.ofSeconds(31));
        assertEquals(SeatStatus.AVAILABLE, service.status("A1"));
        assertDoesNotThrow(() -> service.lock("u2", Set.of("A1")));
    }

    private ShowSeatService service(Clock clock) { return new ShowSeatService(List.of(new Seat("A1"), new Seat("A2")), Duration.ofSeconds(30), clock); }

    private static final class MutableClock extends Clock {
        private final AtomicLong millis = new AtomicLong();
        void advance(Duration duration) { millis.addAndGet(duration.toMillis()); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis.get()); }
        @Override public long millis() { return millis.get(); }
    }
}
