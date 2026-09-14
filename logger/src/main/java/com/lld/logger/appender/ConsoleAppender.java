package com.lld.logger.appender;

import com.lld.logger.format.LogFormatter;
import com.lld.logger.model.LogEvent;
import java.io.PrintStream;
import java.util.Objects;

public final class ConsoleAppender implements LogAppender {
    private final PrintStream output;
    private final LogFormatter formatter;
    public ConsoleAppender(PrintStream output, LogFormatter formatter) {
        this.output = Objects.requireNonNull(output); this.formatter = Objects.requireNonNull(formatter);
    }
    @Override public synchronized void append(LogEvent event) { output.println(formatter.format(event)); }
}
