package com.lld.parkinglot.model;

import java.time.Instant;

public record ParkingTicket(String id, Vehicle vehicle, String spotId, Instant entryTime) {}
