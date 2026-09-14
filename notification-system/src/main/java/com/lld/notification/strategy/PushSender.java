package com.lld.notification.strategy;

import com.lld.notification.model.Channel;
import com.lld.notification.model.Notification;
import java.util.function.Consumer;

public final class PushSender implements NotificationSender {
    private final Consumer<Notification> gateway;
    public PushSender(Consumer<Notification> gateway) { this.gateway = gateway; }
    public Channel channel() { return Channel.PUSH; }
    public void send(Notification notification) { gateway.accept(notification); }
}
