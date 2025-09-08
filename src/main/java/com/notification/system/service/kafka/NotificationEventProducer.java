package com.notification.system.service.kafka;

import com.notification.system.config.KafkaConfig;
import com.notification.system.model.dto.event.NotificationEvent;
import com.notification.system.model.enums.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name = "notification.kafka.enabled", havingValue = "true")
public class NotificationEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventProducer.class);

    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void publishNotificationEvent(NotificationEvent event) {
        String topic = selectTopic(event);
        String key = generateKey(event);
        
        logger.info("📤 Publishing notification event to Kafka: {} -> Topic: {}", 
                   event.getEventId(), topic);

        CompletableFuture<SendResult<String, NotificationEvent>> future = 
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("✅ Successfully published event {} to partition {} with offset {}",
                           event.getEventId(),
                           result.getRecordMetadata().partition(),
                           result.getRecordMetadata().offset());
            } else {
                logger.error("❌ Failed to publish event {}: {}", 
                           event.getEventId(), exception.getMessage(), exception);
            }
        });
    }

    public void publishRetryEvent(NotificationEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setEventType("NOTIFICATION_RETRY");
        
        logger.info("🔄 Publishing retry notification event: {} (Retry count: {})", 
                   event.getEventId(), event.getRetryCount());

        CompletableFuture<SendResult<String, NotificationEvent>> future = 
                kafkaTemplate.send(KafkaConfig.NOTIFICATION_RETRY_TOPIC, generateKey(event), event);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("✅ Successfully published retry event {} to retry topic", 
                           event.getEventId());
            } else {
                logger.error("❌ Failed to publish retry event {}: {}", 
                           event.getEventId(), exception.getMessage(), exception);
            }
        });
    }

    public void publishToDlq(NotificationEvent event, String reason) {
        event.setEventType("NOTIFICATION_DLQ");
        if (event.getMetadata() == null) {
            event.setMetadata(new java.util.HashMap<>());
        }
        event.getMetadata().put("dlq_reason", reason);
        event.getMetadata().put("dlq_timestamp", java.time.LocalDateTime.now().toString());
        
        logger.warn("⚠️ Publishing event to DLQ: {} - Reason: {}", event.getEventId(), reason);

        CompletableFuture<SendResult<String, NotificationEvent>> future = 
                kafkaTemplate.send(KafkaConfig.NOTIFICATION_DLQ_TOPIC, generateKey(event), event);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                logger.info("✅ Successfully published event {} to DLQ", event.getEventId());
            } else {
                logger.error("❌ Failed to publish event {} to DLQ: {}", 
                           event.getEventId(), exception.getMessage(), exception);
            }
        });
    }

    private String selectTopic(NotificationEvent event) {
        if (event.getPriority() == Priority.HIGH) {
            return KafkaConfig.NOTIFICATION_HIGH_PRIORITY_TOPIC;
        }
        return KafkaConfig.NOTIFICATION_TOPIC;
    }

    private String generateKey(NotificationEvent event) {
        return event.getUserId() != null ? event.getUserId().toString() : "unknown";
    }
}