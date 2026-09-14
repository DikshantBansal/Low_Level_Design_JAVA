package com.lld.moviebooking.model;

public record Seat(String id) {
    public Seat { if (id == null || id.isBlank()) throw new IllegalArgumentException("seat id is required"); }
}
