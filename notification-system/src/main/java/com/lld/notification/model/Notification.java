package com.lld.notification.model;

import java.util.Map;

public record Notification(String recipientId, String subject, String body, Map<String, String> attributes) {
    public Notification {
        if (recipientId == null || recipientId.isBlank()) throw new IllegalArgumentException("recipientId is required");
        if (body == null || body.isBlank()) throw new IllegalArgumentException("body is required");
        subject = subject == null ? "" : subject;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
