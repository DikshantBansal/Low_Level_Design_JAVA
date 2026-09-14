package com.lld.logger.service;

import com.lld.logger.appender.LogAppender;
import com.lld.logger.model.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

/** Immutable, thread-safe logger configured with a severity threshold and appenders. */
public final class Logger {
    private final String name;
    private final LogLevel threshold;
    private final List<LogAppender> appenders;
    private final Clock clock;

    public Logger(String name, LogLevel threshold, Collection<LogAppender> appenders) {
        this(name, threshold, appenders, Clock.systemUTC());
    }
    public Logger(String name, LogLevel threshold, Collection<LogAppender> appenders, Clock clock) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (appenders == null || appenders.isEmpty()) throw new IllegalArgumentException("at least one appender is required");
        this.name = name; this.threshold = Objects.requireNonNull(threshold); this.appenders = List.copyOf(appenders); this.clock = Objects.requireNonNull(clock);
    }

    public void debug(String message) { log(LogLevel.DEBUG, message, null, Map.of()); }
    public void info(String message) { log(LogLevel.INFO, message, null, Map.of()); }
    public void warn(String message) { log(LogLevel.WARN, message, null, Map.of()); }
    public void error(String message, Throwable throwable) { log(LogLevel.ERROR, message, throwable, Map.of()); }

    public void log(LogLevel level, String message, Throwable throwable, Map<String, String> context) {
        Objects.requireNonNull(level);
        if (level.ordinal() < threshold.ordinal()) return;
        LogEvent event = new LogEvent(Instant.now(clock), level, name, message, Thread.currentThread().getName(), throwable, context);
        appenders.forEach(appender -> appender.append(event));
    }
}
