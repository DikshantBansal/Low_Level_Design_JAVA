package com.lld.taskscheduler.model;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public final class TaskHandle {
    private final UUID id;
    private final AtomicReference<TaskStatus> status = new AtomicReference<>(TaskStatus.SCHEDULED);
    private final CompletableFuture<Void> completion = new CompletableFuture<>();
    private volatile Future<?> scheduledFuture;

    public TaskHandle(UUID id) { this.id = id; }
    public UUID id() { return id; }
    public TaskStatus status() { return status.get(); }
    public CompletableFuture<Void> completion() { return completion; }

    public boolean cancel() {
        if (!status.compareAndSet(TaskStatus.SCHEDULED, TaskStatus.CANCELLED)) return false;
        Future<?> future = scheduledFuture;
        if (future != null) future.cancel(false);
        completion.cancel(false);
        return true;
    }

    public boolean markRunning() { return status.compareAndSet(TaskStatus.SCHEDULED, TaskStatus.RUNNING); }
    public void markCompleted() { status.set(TaskStatus.COMPLETED); completion.complete(null); }
    public void markFailed(Throwable error) { status.set(TaskStatus.FAILED); completion.completeExceptionally(error); }
    public void attach(Future<?> future) { this.scheduledFuture = future; if (status.get() == TaskStatus.CANCELLED) future.cancel(false); }
}
