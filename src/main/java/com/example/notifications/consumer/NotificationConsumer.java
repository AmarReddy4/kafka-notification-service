package com.example.notifications.consumer;

import com.example.notifications.config.KafkaConfig;
import com.example.notifications.model.Notification;
import com.example.notifications.model.UserEvent;
import com.example.notifications.producer.EventProducer;
import com.example.notifications.service.NotificationRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final EventProducer eventProducer;
    private final NotificationRouter notificationRouter;

    @KafkaListener(
            topics = KafkaConfig.USER_EVENTS_TOPIC,
            groupId = "notification-processor",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUserEvent(UserEvent event) {
        log.info("Received user event: type={}, userId={}", event.getEventType(), event.getUserId());

        try {
            Notification notification = notificationRouter.route(event);
            eventProducer.publishNotification(notification);
            log.info("Notification generated and published for userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user event for userId={}: {}. Sending to DLQ.",
                    event.getUserId(), e.getMessage());
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaConfig.NOTIFICATION_DLQ_TOPIC,
            groupId = "dlq-processor"
    )
    public void consumeDlq(UserEvent event) {
        log.warn("Received DLQ event: type={}, userId={}. Manual intervention may be required.",
                event.getEventType(), event.getUserId());
    }
}
