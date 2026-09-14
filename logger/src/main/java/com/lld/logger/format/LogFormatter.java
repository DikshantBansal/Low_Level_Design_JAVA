package com.lld.logger.format;

import com.lld.logger.model.LogEvent;

@FunctionalInterface
public interface LogFormatter { String format(LogEvent event); }
