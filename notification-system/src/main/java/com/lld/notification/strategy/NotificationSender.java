package com.lld.notification.strategy;

import com.lld.notification.model.Channel;
import com.lld.notification.model.Notification;

public interface NotificationSender {
    Channel channel();
    void send(Notification notification);
}
