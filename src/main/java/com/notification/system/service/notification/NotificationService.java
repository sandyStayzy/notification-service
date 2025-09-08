package com.notification.system.service.notification;

import com.notification.system.model.dto.request.NotificationRequest;
import com.notification.system.model.dto.response.NotificationResponse;
import com.notification.system.model.dto.event.NotificationEvent;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.User;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.repository.NotificationRepository;
import com.notification.system.repository.UserRepository;
import com.notification.system.service.scheduler.NotificationSchedulerService;
import com.notification.system.service.kafka.NotificationEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationSchedulerService schedulerService;
    
    @Autowired
    private NotificationProcessor notificationProcessor;

    @Autowired(required = false)
    private NotificationEventProducer eventProducer;

    @Value("${notification.kafka.enabled:false}")
    private boolean kafkaEnabled;
    
    public NotificationResponse sendNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        
        Notification notification = new Notification(user, request.getTitle(), request.getContent(), 
                request.getChannelType(), request.getPriority());
        notification.setMetadata(request.getMetadata());
        
        if (request.getScheduledAt() != null) {
            notification.setScheduledAt(request.getScheduledAt());
            notification.setStatus(NotificationStatus.SCHEDULED);
        } else {
            notification.setStatus(NotificationStatus.PENDING);
        }
        
        notification = notificationRepository.save(notification);
        
        // Handle scheduling or immediate sending
        if (notification.getStatus() == NotificationStatus.SCHEDULED) {
            // Schedule with Quartz
            schedulerService.scheduleNotification(notification);
        } else {
            if (kafkaEnabled && eventProducer != null) {
                // Publish to Kafka for asynchronous processing
                publishNotificationEvent(notification);
            } else {
                // Fallback to synchronous processing when Kafka is disabled
                notificationProcessor.processNotificationWithRetry(notification);
            }
        }
        
        return mapToResponse(notification);
    }
    
    public Optional<NotificationResponse> getNotification(Long id) {
        return notificationRepository.findById(id)
                .map(this::mapToResponse);
    }
    
    public Page<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUser(user, pageable)
                .map(this::mapToResponse);
    }
    
    
    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUser().getId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setChannelType(notification.getChannelType());
        response.setPriority(notification.getPriority());
        response.setStatus(notification.getStatus());
        response.setMetadata(notification.getMetadata());
        response.setScheduledAt(notification.getScheduledAt());
        response.setSentAt(notification.getSentAt());
        response.setRetryCount(notification.getRetryCount());
        response.setErrorMessage(notification.getErrorMessage());
        response.setCreatedAt(notification.getCreatedAt());
        response.setUpdatedAt(notification.getUpdatedAt());
        return response;
    }

    private void publishNotificationEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent(
                notification.getId(),
                notification.getUser().getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getChannelType(),
                notification.getPriority()
        );
        
        event.setMetadata(notification.getMetadata());
        event.setScheduledAt(notification.getScheduledAt());
        
        eventProducer.publishNotificationEvent(event);
    }
}