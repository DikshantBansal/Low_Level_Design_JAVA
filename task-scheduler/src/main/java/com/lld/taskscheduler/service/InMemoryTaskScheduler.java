package com.lld.taskscheduler.service;

import com.lld.taskscheduler.model.TaskHandle;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Thread-safe one-time task scheduler backed by a bounded number of worker threads. */
public final class InMemoryTaskScheduler implements TaskScheduler {
    private final ScheduledExecutorService executor;
    private final Clock clock;
    private final Map<UUID, TaskHandle> pending = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    public InMemoryTaskScheduler(int workerCount) { this(workerCount, Clock.systemUTC()); }

    public InMemoryTaskScheduler(int workerCount, Clock clock) {
        if (workerCount <= 0) throw new IllegalArgumentException("workerCount must be positive");
        this.clock = Objects.requireNonNull(clock);
        this.executor = Executors.newScheduledThreadPool(workerCount, runnable -> {
            Thread thread = new Thread(runnable, "task-scheduler-worker"); thread.setDaemon(true); return thread;
        });
    }

    @Override
    public TaskHandle schedule(String name, Runnable task, Instant executeAt) {
        if (closed.get()) throw new IllegalStateException("scheduler is closed");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        Objects.requireNonNull(task); Objects.requireNonNull(executeAt);
        TaskHandle handle = new TaskHandle(UUID.randomUUID());
        pending.put(handle.id(), handle);
        long delayNanos = Math.max(0, Duration.between(Instant.now(clock), executeAt).toNanos());
        try {
            ScheduledFuture<?> future = executor.schedule(() -> execute(handle, task), delayNanos, TimeUnit.NANOSECONDS);
            handle.attach(future);
            return handle;
        } catch (RejectedExecutionException error) {
            pending.remove(handle.id()); handle.cancel(); throw new IllegalStateException("scheduler is closed", error);
        }
    }

    private void execute(TaskHandle handle, Runnable task) {
        if (!handle.markRunning()) { pending.remove(handle.id()); return; }
        try { task.run(); handle.markCompleted(); }
        catch (Throwable error) { handle.markFailed(error); }
        finally { pending.remove(handle.id()); }
    }

    public int pendingTaskCount() { return pending.size(); }

    @Override public void close() {
        if (closed.compareAndSet(false, true)) {
            pending.values().forEach(TaskHandle::cancel); pending.clear(); executor.shutdownNow();
        }
    }
}
