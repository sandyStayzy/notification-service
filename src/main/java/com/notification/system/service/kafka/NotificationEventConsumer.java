package com.notification.system.service.kafka;

import com.notification.system.config.KafkaConfig;
import com.notification.system.model.dto.event.NotificationEvent;
import com.notification.system.model.entity.Notification;
import com.notification.system.repository.NotificationRepository;
import com.notification.system.service.notification.NotificationProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnProperty(name = "notification.kafka.enabled", havingValue = "true")
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Autowired
    private NotificationProcessor notificationProcessor;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired(required = false)
    private NotificationEventProducer eventProducer;

    @KafkaListener(topics = KafkaConfig.NOTIFICATION_TOPIC, groupId = "notification-service")
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR
    )
    public void consumeNotificationEvent(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("📥 Received notification event from topic: {} - Event: {}", topic, event.getEventId());
        
        try {
            processNotificationEvent(event);
            logger.info("✅ Successfully processed event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("❌ Error processing notification event {}: {}", 
                        event.getEventId(), e.getMessage(), e);
            
            handleEventProcessingError(event, e);
            throw e; // Rethrow to trigger retry mechanism
        }
    }

    @KafkaListener(topics = KafkaConfig.NOTIFICATION_HIGH_PRIORITY_TOPIC, groupId = "notification-service-high-priority")
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 500, multiplier = 2.0),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    public void consumeHighPriorityNotificationEvent(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        logger.info("🔥 Received HIGH PRIORITY notification event from topic: {} - Event: {}", 
                   topic, event.getEventId());
        
        try {
            processNotificationEvent(event);
            logger.info("✅ Successfully processed high priority event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("❌ Error processing high priority notification event {}: {}", 
                        event.getEventId(), e.getMessage(), e);
            
            handleEventProcessingError(event, e);
            throw e;
        }
    }

    @KafkaListener(topics = KafkaConfig.NOTIFICATION_RETRY_TOPIC, groupId = "notification-service-retry")
    public void consumeRetryNotificationEvent(@Payload NotificationEvent event) {
        
        logger.info("🔄 Received RETRY notification event - Event: {} (Retry count: {})", 
                   event.getEventId(), event.getRetryCount());
        
        if (event.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
            logger.warn("⚠️ Max retry attempts reached for event: {}, sending to DLQ", event.getEventId());
            if (eventProducer != null) {
                eventProducer.publishToDlq(event, "Max retry attempts exceeded");
            }
            return;
        }
        
        try {
            processNotificationEvent(event);
            logger.info("✅ Successfully processed retry event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("❌ Error processing retry notification event {}: {}", 
                        event.getEventId(), e.getMessage(), e);
            
            // Schedule next retry with exponential backoff
            scheduleRetryWithDelay(event, e);
        }
    }

    @KafkaListener(topics = KafkaConfig.NOTIFICATION_DLQ_TOPIC, groupId = "notification-service-dlq")
    public void consumeDlqNotificationEvent(@Payload NotificationEvent event) {
        
        logger.warn("💀 Received DLQ notification event - Event: {} - Reason: {}", 
                   event.getEventId(), 
                   event.getMetadata() != null ? event.getMetadata().get("dlq_reason") : "Unknown");
        
        // For DLQ events, we can implement alerting, logging to external systems, etc.
        // Message is automatically acknowledged for DLQ processing
    }

    private void processNotificationEvent(NotificationEvent event) {
        logger.info("🔄 Processing notification event: {} for user: {}", 
                   event.getEventId(), event.getUserId());

        Optional<Notification> notificationOpt = notificationRepository.findByIdWithUser(event.getNotificationId());
        
        if (notificationOpt.isEmpty()) {
            logger.error("❌ Notification not found for event: {} - ID: {}", 
                        event.getEventId(), event.getNotificationId());
            throw new RuntimeException("Notification not found: " + event.getNotificationId());
        }

        Notification notification = notificationOpt.get();
        
        boolean success = notificationProcessor.processNotification(notification);
        
        if (!success) {
            throw new RuntimeException("Failed to process notification: " + event.getNotificationId());
        }
        
        logger.info("✅ Successfully processed notification event: {}", event.getEventId());
    }

    private void handleEventProcessingError(NotificationEvent event, Exception error) {
        if (event.getMetadata() == null) {
            event.setMetadata(new java.util.HashMap<>());
        }
        event.getMetadata().put("last_error", error.getMessage());
        event.getMetadata().put("error_timestamp", java.time.LocalDateTime.now().toString());
    }

    private void scheduleRetryWithDelay(NotificationEvent event, Exception error) {
        int retryCount = event.getRetryCount();
        long delayMs = (long) (1000 * Math.pow(2, retryCount)); // Exponential backoff: 1s, 2s, 4s, 8s...
        
        logger.info("⏰ Scheduling retry for event: {} in {} ms", event.getEventId(), delayMs);
        
        // In a production environment, you might use a delay queue or scheduled executor
        // For now, we'll just publish to retry topic after a brief delay
        try {
            Thread.sleep(Math.min(delayMs, 10000)); // Cap at 10 seconds
            if (eventProducer != null) {
                eventProducer.publishRetryEvent(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Retry scheduling interrupted for event: {}", event.getEventId());
        }
    }
}