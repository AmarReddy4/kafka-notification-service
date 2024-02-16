package com.example.notifications.consumer;

import com.example.notifications.config.KafkaConfig;
import com.example.notifications.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class NotificationSender {

    private final List<Notification> sentNotifications = Collections.synchronizedList(new ArrayList<>());

    @KafkaListener(
            topics = KafkaConfig.NOTIFICATIONS_TOPIC,
            groupId = "notification-sender",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void sendNotification(Notification notification) {
        log.info("Sending notification: channel={}, userId={}, subject={}",
                notification.getChannel(), notification.getUserId(), notification.getSubject());

        try {
            simulateSend(notification);
            notification.setStatus(Notification.Status.SENT);
            sentNotifications.add(notification);
            log.info("Notification sent successfully: channel={}, userId={}",
                    notification.getChannel(), notification.getUserId());
        } catch (Exception e) {
            notification.setStatus(Notification.Status.FAILED);
            sentNotifications.add(notification);
            log.error("Failed to send notification: channel={}, userId={}, error={}",
                    notification.getChannel(), notification.getUserId(), e.getMessage());
        }
    }

    public List<Notification> getSentNotifications() {
        return Collections.unmodifiableList(sentNotifications);
    }

    private void simulateSend(Notification notification) {
        switch (notification.getChannel()) {
            case EMAIL -> log.info("[EMAIL] To: user-{} | Subject: {} | Body: {}",
                    notification.getUserId(), notification.getSubject(), notification.getBody());
            case SMS -> log.info("[SMS] To: user-{} | Message: {}",
                    notification.getUserId(), notification.getBody());
        }
    }
}
