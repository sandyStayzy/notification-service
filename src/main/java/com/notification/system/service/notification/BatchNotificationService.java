package com.notification.system.service.notification;

import com.notification.system.model.dto.request.BatchNotificationRequest;
import com.notification.system.model.dto.response.BatchNotificationResponse;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.User;
import com.notification.system.model.enums.BatchStatus;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.repository.NotificationRepository;
import com.notification.system.repository.UserRepository;
import com.notification.system.service.scheduler.NotificationSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
public class BatchNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(BatchNotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSchedulerService schedulerService;

    @Autowired
    private NotificationProcessor notificationProcessor;

    public BatchNotificationResponse processBatchNotification(BatchNotificationRequest request) {
        String batchId = "batch_" + System.currentTimeMillis();
        LocalDateTime startTime = LocalDateTime.now();

        logger.info("📦 Starting batch notification processing: {} for {} users", 
                   batchId, request.getUserIds().size());

        BatchNotificationResponse response = new BatchNotificationResponse(batchId, request.getUserIds().size());
        response.setStatus(BatchStatus.PROCESSING);

        try {
            List<User> users = fetchValidUsers(request.getUserIds(), response);
            
            if (users.isEmpty()) {
                response.setStatus(BatchStatus.FAILED);
                response.setErrorMessage("No valid users found");
                response.setCompletedAt(LocalDateTime.now());
                response.setProcessingTimeMs(calculateProcessingTime(startTime));
                return response;
            }

            List<Notification> notifications = createNotifications(request, users);
            notificationRepository.saveAll(notifications);

            List<BatchNotificationResponse.NotificationResult> results;
            
            if (request.getScheduledAt() != null) {
                results = scheduleNotifications(notifications);
            } else {
                results = processNotifications(notifications, request.getBatchSettings());
            }

            updateBatchResponse(response, results, startTime);
            
            logger.info("✅ Batch notification processing completed: {} - Success: {}, Failed: {}", 
                       batchId, response.getSuccessCount(), response.getFailureCount());

        } catch (Exception e) {
            logger.error("❌ Batch notification processing failed: {}", e.getMessage(), e);
            response.setStatus(BatchStatus.FAILED);
            response.setErrorMessage("Processing failed: " + e.getMessage());
            response.setCompletedAt(LocalDateTime.now());
            response.setProcessingTimeMs(calculateProcessingTime(startTime));
        }

        return response;
    }

    private List<User> fetchValidUsers(List<Long> userIds, BatchNotificationResponse response) {
        List<User> users = userRepository.findAllById(userIds);
        Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
        
        List<Long> missingIds = userIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());
        
        if (!missingIds.isEmpty()) {
            logger.warn("⚠️ Missing users for IDs: {}", missingIds);
        }
        
        logger.info("👥 Found {} valid users out of {} requested", users.size(), userIds.size());
        return users;
    }

    private List<Notification> createNotifications(BatchNotificationRequest request, List<User> users) {
        return users.stream().map(user -> {
            Notification notification = new Notification(
                user, request.getTitle(), request.getContent(),
                request.getChannelType(), request.getPriority()
            );
            notification.setMetadata(request.getMetadata());
            
            if (request.getScheduledAt() != null) {
                notification.setScheduledAt(request.getScheduledAt());
                notification.setStatus(NotificationStatus.SCHEDULED);
            } else {
                notification.setStatus(NotificationStatus.PENDING);
            }
            
            return notification;
        }).collect(Collectors.toList());
    }

    private List<BatchNotificationResponse.NotificationResult> scheduleNotifications(List<Notification> notifications) {
        logger.info("📅 Scheduling {} notifications", notifications.size());
        
        return notifications.stream().map(notification -> {
            try {
                schedulerService.scheduleNotification(notification);
                return new BatchNotificationResponse.NotificationResult(
                    notification.getUser().getId(),
                    notification.getId(),
                    true,
                    "Scheduled successfully for " + notification.getScheduledAt()
                );
            } catch (Exception e) {
                logger.error("❌ Failed to schedule notification for user {}: {}", 
                           notification.getUser().getId(), e.getMessage());
                return new BatchNotificationResponse.NotificationResult(
                    notification.getUser().getId(),
                    notification.getId(),
                    false,
                    "Scheduling failed: " + e.getMessage()
                );
            }
        }).collect(Collectors.toList());
    }

    private List<BatchNotificationResponse.NotificationResult> processNotifications(
            List<Notification> notifications, BatchNotificationRequest.BatchSettings settings) {
        
        List<List<Notification>> batches = createBatches(notifications, settings.getBatchSize());
        List<BatchNotificationResponse.NotificationResult> allResults = Collections.synchronizedList(new ArrayList<>());
        
        logger.info("🔄 Processing {} notifications in {} batches", notifications.size(), batches.size());

        if (settings.getParallelProcessing()) {
            processInParallel(batches, settings, allResults);
        } else {
            processSequentially(batches, settings, allResults);
        }

        return allResults;
    }

    private List<List<Notification>> createBatches(List<Notification> notifications, int batchSize) {
        List<List<Notification>> batches = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i += batchSize) {
            batches.add(notifications.subList(i, Math.min(i + batchSize, notifications.size())));
        }
        return batches;
    }

    private void processInParallel(List<List<Notification>> batches, 
                                  BatchNotificationRequest.BatchSettings settings,
                                  List<BatchNotificationResponse.NotificationResult> allResults) {
        
        AtomicInteger batchCounter = new AtomicInteger(0);
        
        List<CompletableFuture<Void>> futures = batches.stream().map(batch -> 
            CompletableFuture.runAsync(() -> {
                int batchNumber = batchCounter.incrementAndGet();
                processBatch(batch, batchNumber, settings.getContinueOnError(), allResults);
                
                if (settings.getDelayBetweenBatches() > 0 && batchNumber < batches.size()) {
                    try {
                        Thread.sleep(settings.getDelayBetweenBatches());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("⚠️ Batch delay interrupted");
                    }
                }
            })
        ).collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processSequentially(List<List<Notification>> batches, 
                                   BatchNotificationRequest.BatchSettings settings,
                                   List<BatchNotificationResponse.NotificationResult> allResults) {
        
        for (int i = 0; i < batches.size(); i++) {
            processBatch(batches.get(i), i + 1, settings.getContinueOnError(), allResults);
            
            if (settings.getDelayBetweenBatches() > 0 && i < batches.size() - 1) {
                try {
                    Thread.sleep(settings.getDelayBetweenBatches());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("⚠️ Batch delay interrupted");
                    break;
                }
            }
        }
    }

    private void processBatch(List<Notification> batch, int batchNumber, boolean continueOnError,
                             List<BatchNotificationResponse.NotificationResult> results) {
        
        logger.info("🔄 Processing batch {} with {} notifications", batchNumber, batch.size());
        
        for (Notification notification : batch) {
            try {
                boolean success = notificationProcessor.processNotification(notification);
                
                BatchNotificationResponse.NotificationResult result = new BatchNotificationResponse.NotificationResult(
                    notification.getUser().getId(),
                    notification.getId(),
                    success,
                    success ? "Sent successfully" : "Failed to send"
                );
                
                results.add(result);
                
                if (!success && !continueOnError) {
                    logger.error("❌ Stopping batch processing due to failure (continueOnError=false)");
                    break;
                }
                
            } catch (Exception e) {
                logger.error("❌ Error processing notification for user {}: {}", 
                           notification.getUser().getId(), e.getMessage());
                
                BatchNotificationResponse.NotificationResult result = new BatchNotificationResponse.NotificationResult(
                    notification.getUser().getId(),
                    notification.getId(),
                    false,
                    "Error: " + e.getMessage()
                );
                
                results.add(result);
                
                if (!continueOnError) {
                    logger.error("❌ Stopping batch processing due to error (continueOnError=false)");
                    break;
                }
            }
        }
    }

    private void updateBatchResponse(BatchNotificationResponse response, 
                                   List<BatchNotificationResponse.NotificationResult> results,
                                   LocalDateTime startTime) {
        
        response.setResults(results);
        response.setCompletedAt(LocalDateTime.now());
        response.setProcessingTimeMs(calculateProcessingTime(startTime));
        
        int successCount = (int) results.stream().filter(BatchNotificationResponse.NotificationResult::isSuccess).count();
        int failureCount = results.size() - successCount;
        
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        if (failureCount == 0) {
            response.setStatus(BatchStatus.COMPLETED);
        } else if (successCount > 0) {
            response.setStatus(BatchStatus.PARTIALLY_FAILED);
        } else {
            response.setStatus(BatchStatus.FAILED);
        }
        
        BatchNotificationResponse.BatchStatistics stats = new BatchNotificationResponse.BatchStatistics();
        stats.setTotalBatches(1);
        stats.setProcessedBatches(1);
        stats.setAverageProcessingTimePerBatch(response.getProcessingTimeMs().doubleValue());
        stats.setSuccessRate(successCount > 0 ? (double) successCount / results.size() * 100 : 0.0);
        
        Map<String, Integer> errorBreakdown = new ConcurrentHashMap<>();
        results.stream()
            .filter(r -> !r.isSuccess())
            .forEach(r -> errorBreakdown.merge(r.getMessage(), 1, Integer::sum));
        stats.setErrorBreakdown(errorBreakdown);
        
        response.setStatistics(stats);
    }

    private long calculateProcessingTime(LocalDateTime startTime) {
        return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
    }
}