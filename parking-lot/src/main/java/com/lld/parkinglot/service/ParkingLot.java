package com.lld.parkinglot.service;

import com.lld.parkinglot.model.ParkingSpot;
import com.lld.parkinglot.model.ParkingTicket;
import com.lld.parkinglot.model.Vehicle;
import com.lld.parkinglot.strategy.SpotAllocationStrategy;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ParkingLot {
    private final Map<String, ParkingSpot> spots;
    private final Map<String, ParkingTicket> activeTickets = new ConcurrentHashMap<>();
    private final SpotAllocationStrategy allocationStrategy;
    private final Clock clock;

    public ParkingLot(Collection<ParkingSpot> spots, SpotAllocationStrategy strategy) {
        this(spots, strategy, Clock.systemUTC());
    }

    public ParkingLot(Collection<ParkingSpot> spots, SpotAllocationStrategy strategy, Clock clock) {
        this.spots = new ConcurrentHashMap<>();
        for (ParkingSpot spot : spots) {
            if (this.spots.putIfAbsent(spot.id(), spot) != null) throw new IllegalArgumentException("duplicate spot: " + spot.id());
        }
        this.allocationStrategy = Objects.requireNonNull(strategy);
        this.clock = Objects.requireNonNull(clock);
    }

    public ParkingTicket park(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);
        while (true) {
            ParkingSpot spot = allocationStrategy.select(spots.values(), vehicle)
                    .orElseThrow(() -> new IllegalStateException("no compatible spot available"));
            if (spot.park(vehicle)) {
                ParkingTicket ticket = new ParkingTicket(UUID.randomUUID().toString(), vehicle, spot.id(), Instant.now(clock));
                activeTickets.put(ticket.id(), ticket);
                return ticket;
            }
        }
    }

    public Vehicle exit(String ticketId) {
        ParkingTicket ticket = activeTickets.remove(ticketId);
        if (ticket == null) throw new IllegalArgumentException("unknown or closed ticket");
        return spots.get(ticket.spotId()).vacate();
    }

    public long availableSpots() { return spots.values().stream().filter(ParkingSpot::isAvailable).count(); }
}
