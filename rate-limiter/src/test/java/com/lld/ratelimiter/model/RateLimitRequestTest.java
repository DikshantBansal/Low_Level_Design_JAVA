package com.lld.ratelimiter.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RateLimitRequestTest {
    @Test
    void defaultsToOnePermit() {
        assertEquals(1, new RateLimitRequest("client-a").permits());
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimitRequest(" "));
        assertThrows(IllegalArgumentException.class, () -> new RateLimitRequest("client-a", 0));
    }
}
