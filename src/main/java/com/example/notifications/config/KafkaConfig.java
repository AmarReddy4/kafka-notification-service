package com.example.notifications.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    public static final String USER_EVENTS_TOPIC = "user-events";
    public static final String NOTIFICATIONS_TOPIC = "notifications";
    public static final String NOTIFICATION_DLQ_TOPIC = "notification-dlq";

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(USER_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(NOTIFICATIONS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationDlqTopic() {
        return TopicBuilder.name(NOTIFICATION_DLQ_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate,
                        (record, ex) -> new org.apache.kafka.common.TopicPartition(
                                NOTIFICATION_DLQ_TOPIC, 0));

        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
    }
}
