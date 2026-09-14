package com.lld.taskscheduler.service;

import com.lld.taskscheduler.model.TaskHandle;
import java.time.Instant;

public interface TaskScheduler extends AutoCloseable {
    TaskHandle schedule(String name, Runnable task, Instant executeAt);
    @Override void close();
}
