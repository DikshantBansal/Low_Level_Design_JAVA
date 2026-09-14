package com.lld.logger.model;

import java.time.Instant;
import java.util.Map;

public record LogEvent(Instant timestamp, LogLevel level, String loggerName, String message,
                       String threadName, Throwable throwable, Map<String, String> context) {
    public LogEvent {
        if (timestamp == null || level == null) throw new IllegalArgumentException("timestamp and level are required");
        if (loggerName == null || loggerName.isBlank()) throw new IllegalArgumentException("loggerName is required");
        if (message == null) throw new IllegalArgumentException("message is required");
        context = context == null ? Map.of() : Map.copyOf(context);
    }
}
