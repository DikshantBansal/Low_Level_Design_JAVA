package com.lld.logger.format;

import com.lld.logger.model.LogEvent;

public final class SimpleLogFormatter implements LogFormatter {
    @Override public String format(LogEvent event) {
        String base = "%s [%s] [%s] %s - %s".formatted(event.timestamp(), event.threadName(), event.level(), event.loggerName(), event.message());
        return event.throwable() == null ? base : base + " | " + event.throwable().getClass().getSimpleName() + ": " + event.throwable().getMessage();
    }
}
