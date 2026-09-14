package com.lld.parkinglot.service;

import com.lld.parkinglot.model.*;
import com.lld.parkinglot.strategy.FirstAvailableSpotStrategy;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ParkingLotTest {
    @Test void allocatesByTypeAndTracksAvailability() {
        ParkingLot lot = new ParkingLot(List.of(new ParkingSpot("C1", VehicleType.CAR), new ParkingSpot("M1", VehicleType.MOTORCYCLE)), new FirstAvailableSpotStrategy());
        Vehicle car = new Vehicle("KA-01-A", VehicleType.CAR);
        ParkingTicket ticket = lot.park(car);
        assertEquals("C1", ticket.spotId());
        assertEquals(1, lot.availableSpots());
        assertSame(car, lot.exit(ticket.id()));
        assertEquals(2, lot.availableSpots());
    }

    @Test void refusesWhenCompatibleCapacityIsExhausted() {
        ParkingLot lot = new ParkingLot(List.of(new ParkingSpot("C1", VehicleType.CAR)), new FirstAvailableSpotStrategy());
        lot.park(new Vehicle("ONE", VehicleType.CAR));
        assertThrows(IllegalStateException.class, () -> lot.park(new Vehicle("TWO", VehicleType.CAR)));
    }
}
