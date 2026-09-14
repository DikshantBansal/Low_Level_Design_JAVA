package com.lld.elevator.strategy;

import com.lld.elevator.model.Direction;
import com.lld.elevator.service.Elevator;
import java.util.Collection;

@FunctionalInterface
public interface ElevatorSelectionStrategy {
    Elevator select(Collection<Elevator> elevators, int floor, Direction direction);
}
