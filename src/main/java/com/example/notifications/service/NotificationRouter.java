package com.example.notifications.service;

import com.example.notifications.model.Notification;
import com.example.notifications.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class NotificationRouter {

    /**
     * Routes a user event to the appropriate notification channel and builds
     * the corresponding notification object.
     */
    public Notification route(UserEvent event) {
        log.debug("Routing event: type={}, userId={}", event.getEventType(), event.getUserId());

        return switch (event.getEventType()) {
            case SIGNUP -> buildSignupNotification(event);
            case PURCHASE -> buildPurchaseNotification(event);
            case PASSWORD_RESET -> buildPasswordResetNotification(event);
        };
    }

    /**
     * Determines the notification channel based on event type.
     */
    public Notification.Channel resolveChannel(UserEvent.EventType eventType) {
        return switch (eventType) {
            case SIGNUP -> Notification.Channel.EMAIL;
            case PURCHASE -> Notification.Channel.EMAIL;
            case PASSWORD_RESET -> Notification.Channel.SMS;
        };
    }

    private Notification buildSignupNotification(UserEvent event) {
        return Notification.builder()
                .userId(event.getUserId())
                .channel(Notification.Channel.EMAIL)
                .subject("Welcome to Our Platform!")
                .body("Hi there! Thank you for signing up. We're excited to have you on board.")
                .status(Notification.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Notification buildPurchaseNotification(UserEvent event) {
        return Notification.builder()
                .userId(event.getUserId())
                .channel(Notification.Channel.EMAIL)
                .subject("Purchase Confirmation")
                .body("Your purchase has been confirmed. Details: " + event.getPayload())
                .status(Notification.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Notification buildPasswordResetNotification(UserEvent event) {
        String resetCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        return Notification.builder()
                .userId(event.getUserId())
                .channel(Notification.Channel.SMS)
                .subject("Password Reset")
                .body("Your password reset code is: " + resetCode)
                .status(Notification.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
