package com.lld.logger.service;

import com.lld.logger.appender.InMemoryAppender;
import com.lld.logger.model.LogLevel;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    @Test void filtersBelowThresholdAndFansOut() {
        InMemoryAppender first = new InMemoryAppender(); InMemoryAppender second = new InMemoryAppender();
        Logger logger = new Logger("orders", LogLevel.INFO, List.of(first, second), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
        logger.debug("hidden"); logger.info("created");
        assertEquals(1, first.events().size()); assertEquals(first.events(), second.events());
        assertEquals(Instant.EPOCH, first.events().getFirst().timestamp());
    }

    @Test void preservesErrorsAndStructuredContext() {
        InMemoryAppender appender = new InMemoryAppender(); Logger logger = new Logger("orders", LogLevel.DEBUG, List.of(appender));
        RuntimeException failure = new RuntimeException("boom");
        logger.log(LogLevel.ERROR, "failed", failure, java.util.Map.of("orderId", "42"));
        assertSame(failure, appender.events().getFirst().throwable());
        assertEquals("42", appender.events().getFirst().context().get("orderId"));
    }
}
