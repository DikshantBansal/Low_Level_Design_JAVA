package com.lld.moviebooking.service;

import com.lld.moviebooking.model.Seat;
import com.lld.moviebooking.model.SeatLock;
import com.lld.moviebooking.model.SeatStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/** Atomic, in-memory seat locking for one show. */
public final class ShowSeatService {
    private final Map<String, SeatState> seats = new HashMap<>();
    private final Map<String, SeatLock> locks = new HashMap<>();
    private final ReentrantLock mutex = new ReentrantLock();
    private final Duration lockDuration;
    private final Clock clock;

    public ShowSeatService(Collection<Seat> seats, Duration lockDuration, Clock clock) {
        if (lockDuration == null || lockDuration.isZero() || lockDuration.isNegative()) throw new IllegalArgumentException("lockDuration must be positive");
        for (Seat seat : seats) {
            if (this.seats.putIfAbsent(seat.id(), new SeatState()) != null) throw new IllegalArgumentException("duplicate seat: " + seat.id());
        }
        this.lockDuration = lockDuration;
        this.clock = Objects.requireNonNull(clock);
    }

    public SeatLock lock(String userId, Set<String> seatIds) {
        if (userId == null || userId.isBlank() || seatIds == null || seatIds.isEmpty()) throw new IllegalArgumentException("user and seats are required");
        mutex.lock();
        try {
            releaseExpired();
            for (String seatId : seatIds) {
                SeatState state = requireSeat(seatId);
                if (state.status != SeatStatus.AVAILABLE) throw new IllegalStateException("seat unavailable: " + seatId);
            }
            SeatLock seatLock = new SeatLock(UUID.randomUUID().toString(), userId, seatIds, Instant.now(clock).plus(lockDuration));
            seatIds.forEach(id -> { SeatState state = seats.get(id); state.status = SeatStatus.LOCKED; state.lockToken = seatLock.token(); });
            locks.put(seatLock.token(), seatLock);
            return seatLock;
        } finally { mutex.unlock(); }
    }

    public void confirm(String lockToken, String userId) {
        mutex.lock();
        try {
            releaseExpired();
            SeatLock seatLock = locks.get(lockToken);
            if (seatLock == null || !seatLock.userId().equals(userId)) throw new IllegalArgumentException("invalid or expired lock");
            seatLock.seatIds().forEach(id -> { SeatState state = seats.get(id); state.status = SeatStatus.BOOKED; state.lockToken = null; });
            locks.remove(lockToken);
        } finally { mutex.unlock(); }
    }

    public SeatStatus status(String seatId) {
        mutex.lock();
        try { releaseExpired(); return requireSeat(seatId).status; }
        finally { mutex.unlock(); }
    }

    private void releaseExpired() {
        Instant now = Instant.now(clock);
        locks.values().removeIf(lock -> {
            if (lock.expiresAt().isAfter(now)) return false;
            lock.seatIds().forEach(id -> { SeatState state = seats.get(id); if (lock.token().equals(state.lockToken)) { state.status = SeatStatus.AVAILABLE; state.lockToken = null; } });
            return true;
        });
    }

    private SeatState requireSeat(String id) {
        SeatState state = seats.get(id);
        if (state == null) throw new IllegalArgumentException("unknown seat: " + id);
        return state;
    }

    private static final class SeatState { private SeatStatus status = SeatStatus.AVAILABLE; private String lockToken; }
}
