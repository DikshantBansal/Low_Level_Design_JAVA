package com.lld.elevator.service;

import com.lld.elevator.model.Direction;
import com.lld.elevator.strategy.ElevatorSelectionStrategy;
import java.util.List;

public final class ElevatorSystem {
    private final List<Elevator> elevators;
    private final ElevatorSelectionStrategy selectionStrategy;

    public ElevatorSystem(List<Elevator> elevators, ElevatorSelectionStrategy strategy) {
        if (elevators == null || elevators.isEmpty()) throw new IllegalArgumentException("at least one elevator is required");
        this.elevators = List.copyOf(elevators); this.selectionStrategy = strategy;
    }

    public Elevator requestPickup(int floor, Direction direction) {
        if (direction == null || direction == Direction.NONE) throw new IllegalArgumentException("pickup direction is required");
        Elevator selected = selectionStrategy.select(elevators, floor, direction);
        selected.requestStop(floor);
        return selected;
    }
}
