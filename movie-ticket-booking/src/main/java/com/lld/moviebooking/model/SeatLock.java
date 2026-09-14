package com.lld.moviebooking.model;

import java.time.Instant;
import java.util.Set;

public record SeatLock(String token, String userId, Set<String> seatIds, Instant expiresAt) {
    public SeatLock { seatIds = Set.copyOf(seatIds); }
}
