package com.example.notifications.producer;

import com.example.notifications.config.KafkaConfig;
import com.example.notifications.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CompletableFuture<SendResult<String, Object>> publishUserEvent(UserEvent event) {
        log.info("Publishing user event: type={}, userId={}", event.getEventType(), event.getUserId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.USER_EVENTS_TOPIC, event.getUserId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish user event for userId={}: {}",
                        event.getUserId(), ex.getMessage());
            } else {
                log.info("User event published successfully: topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }

    public void publishNotification(com.example.notifications.model.Notification notification) {
        log.info("Publishing notification: channel={}, userId={}",
                notification.getChannel(), notification.getUserId());

        kafkaTemplate.send(KafkaConfig.NOTIFICATIONS_TOPIC, notification.getUserId(), notification);
    }
}
