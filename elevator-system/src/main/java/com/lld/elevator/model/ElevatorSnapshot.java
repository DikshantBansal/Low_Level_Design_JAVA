package com.lld.elevator.model;

import java.util.Set;

public record ElevatorSnapshot(String id, int currentFloor, Direction direction, ElevatorState state, Set<Integer> pendingStops) {}
