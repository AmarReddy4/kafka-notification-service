package com.example.notifications.consumer;

import com.example.notifications.config.KafkaConfig;
import com.example.notifications.model.Notification;
import com.example.notifications.model.UserEvent;
import com.example.notifications.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EventProducer eventProducer;

    @KafkaListener(
            topics = KafkaConfig.USER_EVENTS_TOPIC,
            groupId = "notification-processor",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUserEvent(UserEvent event) {
        log.info("Received user event: type={}, userId={}", event.getEventType(), event.getUserId());

        try {
            Notification notification = buildNotification(event);
            eventProducer.publishNotification(notification);
            log.info("Notification generated and published for userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user event for userId={}: {}", event.getUserId(), e.getMessage());
            throw e;
        }
    }

    private Notification buildNotification(UserEvent event) {
        return switch (event.getEventType()) {
            case SIGNUP -> Notification.builder()
                    .userId(event.getUserId())
                    .channel(Notification.Channel.EMAIL)
                    .subject("Welcome to Our Platform!")
                    .body("Hi there! Thank you for signing up. We're excited to have you on board.")
                    .status(Notification.Status.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            case PURCHASE -> Notification.builder()
                    .userId(event.getUserId())
                    .channel(Notification.Channel.EMAIL)
                    .subject("Purchase Confirmation")
                    .body("Your purchase has been confirmed. Details: " + event.getPayload())
                    .status(Notification.Status.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            case PASSWORD_RESET -> Notification.builder()
                    .userId(event.getUserId())
                    .channel(Notification.Channel.SMS)
                    .subject("Password Reset")
                    .body("Your password reset code is: " + generateResetCode())
                    .status(Notification.Status.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
        };
    }

    private String generateResetCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
