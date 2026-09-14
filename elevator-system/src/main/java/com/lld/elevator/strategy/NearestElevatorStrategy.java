package com.lld.elevator.strategy;

import com.lld.elevator.model.*;
import com.lld.elevator.service.Elevator;
import java.util.Collection;
import java.util.Comparator;

/** Prefers cars already moving toward the caller, then the nearest available car. */
public final class NearestElevatorStrategy implements ElevatorSelectionStrategy {
    public Elevator select(Collection<Elevator> elevators, int floor, Direction requestedDirection) {
        return elevators.stream().filter(e -> e.snapshot().state() != ElevatorState.MAINTENANCE)
                .min(Comparator.comparingInt(e -> score(e.snapshot(), floor, requestedDirection)))
                .orElseThrow(() -> new IllegalStateException("no elevator available"));
    }

    private int score(ElevatorSnapshot car, int floor, Direction requestDirection) {
        int distance = Math.abs(car.currentFloor() - floor);
        boolean onTheWay = car.direction() == requestDirection &&
                ((requestDirection == Direction.UP && car.currentFloor() <= floor) || (requestDirection == Direction.DOWN && car.currentFloor() >= floor));
        return distance + (car.state() == ElevatorState.IDLE || onTheWay ? 0 : 10_000);
    }
}
