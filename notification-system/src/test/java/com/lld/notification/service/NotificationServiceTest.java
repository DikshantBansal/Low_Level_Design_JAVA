package com.lld.notification.service;

import com.lld.notification.model.*;
import com.lld.notification.strategy.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    @Test void routesToSelectedStrategiesAndNotifiesObservers() {
        List<Channel> sent = new ArrayList<>();
        NotificationService service = new NotificationService(List.of(
                new EmailSender(n -> sent.add(Channel.EMAIL)), new SmsSender(n -> sent.add(Channel.SMS)), new PushSender(n -> sent.add(Channel.PUSH))));
        List<Channel> observed = new ArrayList<>();
        service.addObserver((notification, channel) -> observed.add(channel));
        service.send(new Notification("u1", "Hello", "Body", Map.of()), List.of(Channel.EMAIL, Channel.PUSH));
        assertEquals(List.of(Channel.EMAIL, Channel.PUSH), sent);
        assertEquals(sent, observed);
    }

    @Test void reportsMissingChannelConfiguration() {
        NotificationService service = new NotificationService(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.send(new Notification("u1", "", "Body", Map.of()), List.of(Channel.SMS)));
    }
}
