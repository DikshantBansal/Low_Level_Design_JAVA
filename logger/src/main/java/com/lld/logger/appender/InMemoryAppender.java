package com.lld.logger.appender;

import com.lld.logger.model.LogEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryAppender implements LogAppender {
    private final List<LogEvent> events = new CopyOnWriteArrayList<>();
    @Override public void append(LogEvent event) { events.add(event); }
    public List<LogEvent> events() { return List.copyOf(events); }
}
