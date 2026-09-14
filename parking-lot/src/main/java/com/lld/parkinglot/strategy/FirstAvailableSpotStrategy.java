package com.lld.parkinglot.strategy;

import com.lld.parkinglot.model.ParkingSpot;
import com.lld.parkinglot.model.Vehicle;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class FirstAvailableSpotStrategy implements SpotAllocationStrategy {
    @Override
    public Optional<ParkingSpot> select(Collection<ParkingSpot> spots, Vehicle vehicle) {
        return spots.stream().filter(ParkingSpot::isAvailable)
                .filter(spot -> spot.supportedType() == vehicle.type())
                .min(Comparator.comparing(ParkingSpot::id));
    }
}
