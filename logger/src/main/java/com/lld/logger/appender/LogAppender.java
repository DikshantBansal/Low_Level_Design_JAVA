package com.lld.logger.appender;

import com.lld.logger.model.LogEvent;

@FunctionalInterface
public interface LogAppender { void append(LogEvent event); }
