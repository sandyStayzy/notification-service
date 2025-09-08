package com.notification.system.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "notification.kafka.enabled", havingValue = "true")
public class KafkaConfig {

    public static final String NOTIFICATION_TOPIC = "notification-events";
    public static final String NOTIFICATION_HIGH_PRIORITY_TOPIC = "notification-events-high-priority";
    public static final String NOTIFICATION_RETRY_TOPIC = "notification-events-retry";
    public static final String NOTIFICATION_DLQ_TOPIC = "notification-events-dlq";
    
    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic notificationHighPriorityTopic() {
        return TopicBuilder.name(NOTIFICATION_HIGH_PRIORITY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic notificationRetryTopic() {
        return TopicBuilder.name(NOTIFICATION_RETRY_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic notificationDlqTopic() {
        return TopicBuilder.name(NOTIFICATION_DLQ_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}