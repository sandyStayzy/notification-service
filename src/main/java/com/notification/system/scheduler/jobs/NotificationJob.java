package com.notification.system.scheduler.jobs;

import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.ScheduledJob;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.repository.NotificationRepository;
import com.notification.system.repository.ScheduledJobRepository;
import com.notification.system.service.notification.NotificationProcessor;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class NotificationJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(NotificationJob.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ScheduledJobRepository scheduledJobRepository;

    @Autowired
    private NotificationProcessor notificationProcessor;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long notificationId = dataMap.getLong("notificationId");
        Long scheduledJobId = dataMap.getLong("scheduledJobId");

        logger.info("🕐 Executing scheduled notification job - NotificationId: {}, JobId: {}", 
                   notificationId, scheduledJobId);

        try {
            // Process the scheduled notification using the dedicated method
            boolean success = notificationProcessor.processScheduledNotificationById(notificationId);
            
            if (success) {
                logger.info("✅ Scheduled notification sent successfully: {}", notificationId);
                markJobCompleted(scheduledJobId, "Notification sent successfully");
            } else {
                logger.error("❌ Failed to send scheduled notification: {}", notificationId);
                markJobCompleted(scheduledJobId, "Failed to send notification");
            }

        } catch (Exception e) {
            logger.error("💥 Error executing scheduled notification job: {}", e.getMessage(), e);
            markJobCompleted(scheduledJobId, "Job execution failed: " + e.getMessage());
            throw new JobExecutionException("Failed to execute notification job", e);
        }
    }

    @Transactional
    private void markJobCompleted(Long scheduledJobId, String message) {
        if (scheduledJobId != null) {
            scheduledJobRepository.findById(scheduledJobId).ifPresent(job -> {
                job.setCompleted(true);
                job.setUpdatedAt(LocalDateTime.now());
                // Store completion message in job data
                if (job.getJobData() == null) {
                    job.setJobData(new java.util.HashMap<>());
                }
                job.getJobData().put("completionMessage", message);
                job.getJobData().put("completedAt", LocalDateTime.now().toString());
                scheduledJobRepository.save(job);
                logger.info("📋 Marked scheduled job {} as completed: {}", scheduledJobId, message);
            });
        }
    }
}