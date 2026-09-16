package com.lld.ratelimiter.strategy;

import java.time.*;
import java.util.concurrent.atomic.AtomicLong;

final class MutableClock extends Clock {
    private final AtomicLong millis = new AtomicLong();
    void advance(Duration duration) { millis.addAndGet(duration.toMillis()); }
    @Override public ZoneId getZone() { return ZoneOffset.UTC; }
    @Override public Clock withZone(ZoneId zone) { return this; }
    @Override public Instant instant() { return Instant.ofEpochMilli(millis.get()); }
    @Override public long millis() { return millis.get(); }
}
