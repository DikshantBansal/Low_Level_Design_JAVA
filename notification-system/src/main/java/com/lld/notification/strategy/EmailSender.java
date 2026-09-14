package com.lld.notification.strategy;

import com.lld.notification.model.Channel;
import com.lld.notification.model.Notification;
import java.util.function.Consumer;

public final class EmailSender implements NotificationSender {
    private final Consumer<Notification> gateway;
    public EmailSender(Consumer<Notification> gateway) { this.gateway = gateway; }
    public Channel channel() { return Channel.EMAIL; }
    public void send(Notification notification) { gateway.accept(notification); }
}
