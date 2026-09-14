package com.lld.parkinglot.model;

public record Vehicle(String registrationNumber, VehicleType type) {
    public Vehicle {
        if (registrationNumber == null || registrationNumber.isBlank()) throw new IllegalArgumentException("registrationNumber is required");
        if (type == null) throw new IllegalArgumentException("type is required");
    }
}
