package com.notification.system.service.notification;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.repository.NotificationRepository;
import com.notification.system.service.channel.NotificationChannel;
import com.notification.system.service.channel.NotificationChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class NotificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationChannelFactory channelFactory;

    public boolean processNotification(Notification notification) {
        logger.debug("🔄 Processing notification: {} (ID: {})", notification.getTitle(), notification.getId());
        
        Optional<NotificationChannel> channelOpt = channelFactory.getChannel(notification.getChannelType());
        
        if (channelOpt.isEmpty()) {
            logger.error("❌ Unsupported channel type: {}", notification.getChannelType());
            updateNotificationStatus(notification, NotificationStatus.FAILED, 
                                   "Unsupported channel type: " + notification.getChannelType());
            return false;
        }
        
        NotificationChannel channel = channelOpt.get();
        logger.debug("📡 Using channel: {}", channel.getChannelName());
        
        try {
            // Update status to PENDING before sending
            notification.setStatus(NotificationStatus.PENDING);
            notificationRepository.save(notification);
            
            NotificationResult result = channel.send(notification);
            
            if (result.isSuccess()) {
                logger.info("✅ Notification sent successfully via {}: {}", 
                           channel.getChannelName(), notification.getTitle());
                updateNotificationStatus(notification, NotificationStatus.SENT, null);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                return true;
            } else {
                logger.error("❌ Channel failed to send notification: {}", result.getMessage());
                updateNotificationStatus(notification, NotificationStatus.FAILED, result.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("💥 Exception during notification processing: {}", e.getMessage(), e);
            updateNotificationStatus(notification, NotificationStatus.FAILED, 
                                   "Channel processing failed: " + e.getMessage());
            return false;
        }
    }

    public void processNotificationWithRetry(Notification notification) {
        boolean success = processNotification(notification);
        
        if (!success && shouldRetry(notification)) {
            scheduleRetry(notification);
        }
    }

    private boolean shouldRetry(Notification notification) {
        final int maxRetries = 3;
        return notification.getRetryCount() < maxRetries && 
               notification.getStatus() == NotificationStatus.FAILED;
    }

    private void scheduleRetry(Notification notification) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        
        // Exponential backoff: 2^retryCount minutes
        int delayMinutes = (int) Math.pow(2, notification.getRetryCount());
        LocalDateTime nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        notification.setNextRetryAt(nextRetryAt);
        notification.setStatus(NotificationStatus.PENDING); // Reset to pending for retry
        
        logger.info("🔄 Scheduling retry #{} for notification {} in {} minutes", 
                   notification.getRetryCount(), notification.getId(), delayMinutes);
        
        notificationRepository.save(notification);
        
        // TODO: Schedule actual retry job with Quartz
        // This could be implemented as a separate retry job or recurring job
    }

    private void updateNotificationStatus(Notification notification, NotificationStatus status, String errorMessage) {
        notification.setStatus(status);
        notification.setErrorMessage(errorMessage);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        logger.debug("📊 Updated notification {} status to: {}", notification.getId(), status);
    }

    @Transactional
    public boolean processScheduledNotificationById(Long notificationId) {
        logger.info("🔍 Looking up scheduled notification ID: {}", notificationId);
        
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            logger.error("❌ Notification not found: {}", notificationId);
            return false;
        }

        Notification notification = notificationOpt.get();
        
        // Check if notification is still scheduled
        if (notification.getStatus() != NotificationStatus.SCHEDULED) {
            logger.warn("⚠️ Notification {} status is {}, skipping execution", 
                       notificationId, notification.getStatus());
            return false;
        }

        logger.info("📤 Processing scheduled notification: {} - {}", 
                   notification.getTitle(), notification.getChannelType());
        
        return processNotification(notification);
    }
}