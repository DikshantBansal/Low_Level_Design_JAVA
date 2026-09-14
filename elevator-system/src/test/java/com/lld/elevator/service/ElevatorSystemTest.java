package com.lld.elevator.service;

import com.lld.elevator.model.*;
import com.lld.elevator.strategy.NearestElevatorStrategy;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ElevatorSystemTest {
    @Test void selectsNearestAvailableElevator() {
        Elevator low = new Elevator("low", 0, 20, 1); Elevator high = new Elevator("high", 0, 20, 15);
        Elevator selected = new ElevatorSystem(List.of(low, high), new NearestElevatorStrategy()).requestPickup(13, Direction.DOWN);
        assertSame(high, selected);
    }

    @Test void movesThroughStopsAndReturnsToIdle() {
        Elevator elevator = new Elevator("E1", 0, 10, 2); elevator.requestStop(4);
        assertEquals(3, elevator.tick().currentFloor());
        ElevatorSnapshot arrived = elevator.tick();
        assertEquals(4, arrived.currentFloor()); assertEquals(ElevatorState.IDLE, arrived.state()); assertEquals(Direction.NONE, arrived.direction());
    }

    @Test void maintenanceCarRejectsRequests() {
        Elevator elevator = new Elevator("E1", 0, 10, 2); elevator.setMaintenance(true);
        assertThrows(IllegalStateException.class, () -> elevator.requestStop(5));
    }
}
