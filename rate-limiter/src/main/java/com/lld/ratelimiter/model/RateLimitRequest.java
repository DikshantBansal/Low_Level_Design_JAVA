package com.lld.ratelimiter.model;

/** A request by one client to consume a number of permits. */
public record RateLimitRequest(String clientId, long permits) {
    public RateLimitRequest {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("clientId must not be blank");
        }
        if (permits <= 0) {
            throw new IllegalArgumentException("permits must be positive");
        }
    }

    public RateLimitRequest(String clientId) {
        this(clientId, 1);
    }
}
