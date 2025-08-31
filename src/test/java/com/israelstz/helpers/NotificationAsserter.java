package com.israelstz.helpers;

import lombok.RequiredArgsConstructor;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class NotificationAsserter {

    private final NotifierMock notifierMock;

    public NotificationAsserter shouldBeSent() {
        var notification = notifierMock.getNotification();
        assertNotNull(notification);
        return this;
    }

    public NotificationAsserter containingText(String expectedNotification) {
        var notification = notifierMock.getNotification();
        assertTrue(notification.contains(expectedNotification));
        return this;
    }

    public void shouldNotBeSent() {
        var notification = notifierMock.getNotification();
        assertNull(notification);
    }
}
