package com.lld.parkinglot.strategy;

import com.lld.parkinglot.model.ParkingSpot;
import com.lld.parkinglot.model.Vehicle;
import java.util.Collection;
import java.util.Optional;

@FunctionalInterface
public interface SpotAllocationStrategy {
    Optional<ParkingSpot> select(Collection<ParkingSpot> spots, Vehicle vehicle);
}
