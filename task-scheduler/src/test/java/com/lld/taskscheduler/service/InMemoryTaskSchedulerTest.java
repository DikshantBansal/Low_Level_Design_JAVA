package com.lld.taskscheduler.service;

import com.lld.taskscheduler.model.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskSchedulerTest {
    @Test void executesDueTaskAndTracksCompletion() throws Exception {
        try (InMemoryTaskScheduler scheduler = new InMemoryTaskScheduler(1)) {
            CountDownLatch ran = new CountDownLatch(1);
            TaskHandle handle = scheduler.schedule("now", ran::countDown, Instant.now());
            assertTrue(ran.await(2, TimeUnit.SECONDS));
            handle.completion().get(2, TimeUnit.SECONDS);
            assertEquals(TaskStatus.COMPLETED, handle.status());
        }
    }

    @Test void cancellationPreventsExecution() {
        AtomicInteger executions = new AtomicInteger();
        try (InMemoryTaskScheduler scheduler = new InMemoryTaskScheduler(1)) {
            TaskHandle handle = scheduler.schedule("later", executions::incrementAndGet, Instant.now().plus(Duration.ofDays(1)));
            assertTrue(handle.cancel()); assertEquals(TaskStatus.CANCELLED, handle.status()); assertEquals(0, executions.get());
        }
    }

    @Test void recordsTaskFailureWithoutKillingScheduler() {
        try (InMemoryTaskScheduler scheduler = new InMemoryTaskScheduler(1)) {
            TaskHandle failed = scheduler.schedule("failure", () -> { throw new IllegalStateException("boom"); }, Instant.now());
            assertThrows(ExecutionException.class, () -> failed.completion().get(2, TimeUnit.SECONDS));
            assertEquals(TaskStatus.FAILED, failed.status());
            TaskHandle next = scheduler.schedule("next", () -> {}, Instant.now());
            assertDoesNotThrow(() -> next.completion().get(2, TimeUnit.SECONDS));
        }
    }
}
