package com.lld.notification.service;

import com.lld.notification.model.Channel;
import com.lld.notification.model.Notification;
import com.lld.notification.strategy.NotificationSender;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class NotificationService {
    private final Map<Channel, NotificationSender> senders = new EnumMap<>(Channel.class);
    private final List<DeliveryObserver> observers = new CopyOnWriteArrayList<>();

    public NotificationService(Collection<NotificationSender> senders) {
        for (NotificationSender sender : senders) {
            if (this.senders.putIfAbsent(sender.channel(), sender) != null) throw new IllegalArgumentException("duplicate sender for " + sender.channel());
        }
    }

    public void addObserver(DeliveryObserver observer) { observers.add(observer); }

    public void send(Notification notification, Collection<Channel> channels) {
        for (Channel channel : channels) {
            NotificationSender sender = senders.get(channel);
            if (sender == null) throw new IllegalArgumentException("no sender configured for " + channel);
            sender.send(notification);
            observers.forEach(observer -> observer.delivered(notification, channel));
        }
    }
}
