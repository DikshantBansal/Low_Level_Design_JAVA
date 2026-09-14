package com.lld.notification.service;

import com.lld.notification.model.Channel;
import com.lld.notification.model.Notification;

@FunctionalInterface
public interface DeliveryObserver {
    void delivered(Notification notification, Channel channel);
}
