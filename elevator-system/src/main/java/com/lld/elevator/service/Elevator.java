package com.lld.elevator.service;

import com.lld.elevator.model.*;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

/** State machine for one elevator; each tick moves at most one floor. */
public final class Elevator {
    private final String id;
    private final int minimumFloor;
    private final int maximumFloor;
    private final NavigableSet<Integer> stops = new TreeSet<>();
    private int currentFloor;
    private Direction direction = Direction.NONE;
    private ElevatorState state = ElevatorState.IDLE;

    public Elevator(String id, int minimumFloor, int maximumFloor, int initialFloor) {
        if (id == null || id.isBlank() || minimumFloor > initialFloor || initialFloor > maximumFloor) throw new IllegalArgumentException("invalid elevator configuration");
        this.id = id; this.minimumFloor = minimumFloor; this.maximumFloor = maximumFloor; this.currentFloor = initialFloor;
    }

    public synchronized void requestStop(int floor) {
        requireFloor(floor);
        if (state == ElevatorState.MAINTENANCE) throw new IllegalStateException("elevator is in maintenance");
        if (floor == currentFloor) return;
        stops.add(floor);
        if (state == ElevatorState.IDLE) { state = ElevatorState.MOVING; direction = floor > currentFloor ? Direction.UP : Direction.DOWN; }
    }

    public synchronized ElevatorSnapshot tick() {
        if (state != ElevatorState.MOVING) return snapshot();
        if (direction == Direction.UP) currentFloor++; else currentFloor--;
        stops.remove(currentFloor);
        chooseNextDirection();
        return snapshot();
    }

    public synchronized void setMaintenance(boolean enabled) {
        if (enabled) { stops.clear(); state = ElevatorState.MAINTENANCE; direction = Direction.NONE; }
        else if (state == ElevatorState.MAINTENANCE) state = ElevatorState.IDLE;
    }

    public synchronized ElevatorSnapshot snapshot() {
        return new ElevatorSnapshot(id, currentFloor, direction, state, Set.copyOf(stops));
    }

    private void chooseNextDirection() {
        if (stops.isEmpty()) { state = ElevatorState.IDLE; direction = Direction.NONE; return; }
        if (direction == Direction.UP && stops.higher(currentFloor) != null) return;
        if (direction == Direction.DOWN && stops.lower(currentFloor) != null) return;
        direction = stops.higher(currentFloor) != null ? Direction.UP : Direction.DOWN;
    }

    private void requireFloor(int floor) {
        if (floor < minimumFloor || floor > maximumFloor) throw new IllegalArgumentException("floor is outside elevator range");
    }
}
