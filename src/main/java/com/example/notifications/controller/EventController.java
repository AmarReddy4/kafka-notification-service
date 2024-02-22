package com.example.notifications.controller;

import com.example.notifications.consumer.NotificationSender;
import com.example.notifications.model.Notification;
import com.example.notifications.model.UserEvent;
import com.example.notifications.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final EventProducer eventProducer;
    private final NotificationSender notificationSender;

    @PostMapping("/events")
    public ResponseEntity<Map<String, String>> publishEvent(@RequestBody UserEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        log.info("Received event request: type={}, userId={}", event.getEventType(), event.getUserId());

        try {
            eventProducer.publishUserEvent(event);
            return ResponseEntity.ok(Map.of(
                    "status", "accepted",
                    "message", "Event published to Kafka",
                    "userId", event.getUserId(),
                    "eventType", event.getEventType().name()
            ));
        } catch (Exception e) {
            log.error("Failed to publish event: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Failed to publish event: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        List<Notification> notifications = notificationSender.getSentNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable String userId) {
        List<Notification> notifications = notificationSender.getSentNotifications().stream()
                .filter(n -> n.getUserId().equals(userId))
                .toList();
        return ResponseEntity.ok(notifications);
    }
}
