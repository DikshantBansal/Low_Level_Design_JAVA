package com.lld.parkinglot.model;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class ParkingSpot {
    private final String id;
    private final VehicleType supportedType;
    private final AtomicReference<Vehicle> occupant = new AtomicReference<>();

    public ParkingSpot(String id, VehicleType supportedType) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id is required");
        this.id = id;
        this.supportedType = Objects.requireNonNull(supportedType);
    }

    public String id() { return id; }
    public VehicleType supportedType() { return supportedType; }
    public boolean isAvailable() { return occupant.get() == null; }
    public Optional<Vehicle> occupant() { return Optional.ofNullable(occupant.get()); }
    public boolean park(Vehicle vehicle) {
        Objects.requireNonNull(vehicle);
        return vehicle.type() == supportedType && occupant.compareAndSet(null, vehicle);
    }
    public Vehicle vacate() {
        Vehicle vehicle = occupant.getAndSet(null);
        if (vehicle == null) throw new IllegalStateException("spot is already empty");
        return vehicle;
    }
}
