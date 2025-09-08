package com.notification.system.service.scheduler;

import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.ScheduledJob;
import com.notification.system.repository.ScheduledJobRepository;
import com.notification.system.scheduler.jobs.NotificationJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduledJobRepository scheduledJobRepository;

    public ScheduledJob scheduleNotification(Notification notification) {
        if (notification.getScheduledAt() == null) {
            throw new IllegalArgumentException("Notification must have scheduledAt date");
        }

        try {
            String jobKey = "notification_" + notification.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);
            String jobGroup = "notification_jobs";

            logger.info("📅 Scheduling notification: {} for {} (UTC)", 
                       notification.getTitle(), notification.getScheduledAt());
            logger.debug("🔍 Current time: {} (UTC)", LocalDateTime.now().atZone(ZoneId.of("UTC")));

            // Create and save scheduled job record first
            ScheduledJob scheduledJob = createScheduledJobRecord(notification, jobKey, jobGroup);
            scheduledJob = scheduledJobRepository.save(scheduledJob);

            // Create job detail with both notification ID and scheduled job ID
            JobDetail jobDetail = JobBuilder.newJob(NotificationJob.class)
                    .withIdentity(jobKey, jobGroup)
                    .withDescription("Scheduled notification: " + notification.getTitle())
                    .usingJobData("notificationId", notification.getId())
                    .usingJobData("scheduledJobId", scheduledJob.getId())
                    .build();

            // Create trigger - treat scheduledAt as UTC time
            Date scheduledDate = Date.from(notification.getScheduledAt()
                    .atZone(ZoneId.of("UTC")).toInstant());

            // Add safety check for scheduling in the past
            Date now = new Date();
            if (scheduledDate.before(now)) {
                logger.warn("⚠️ Scheduled time {} is in the past (current: {}). Adding 30 second buffer.", 
                           scheduledDate, now);
                scheduledDate = new Date(System.currentTimeMillis() + 30000); // 30 seconds from now
            }

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobKey + "_trigger", jobGroup)
                    .withDescription("Trigger for notification: " + notification.getTitle())
                    .startAt(scheduledDate)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withMisfireHandlingInstructionNextWithExistingCount()) // Don't fire immediately
                    .build();

            // Schedule the job
            scheduler.scheduleJob(jobDetail, trigger);

            // Update job detail with scheduled job ID (but don't add it separately since it's already scheduled)
            // The job is already scheduled with the trigger above

            logger.info("✅ Successfully scheduled notification job: {} (Job ID: {})", 
                       jobKey, scheduledJob.getId());

            return scheduledJob;

        } catch (SchedulerException e) {
            logger.error("❌ Failed to schedule notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to schedule notification", e);
        }
    }

    public boolean cancelScheduledNotification(Long notificationId) {
        try {
            ScheduledJob scheduledJob = scheduledJobRepository
                    .findByNotificationIdAndIsCompleted(notificationId, false)
                    .orElse(null);

            if (scheduledJob == null) {
                logger.warn("⚠️ No active scheduled job found for notification: {}", notificationId);
                return false;
            }

            JobKey jobKey = new JobKey(scheduledJob.getJobKey(), scheduledJob.getJobGroup());
            boolean deleted = scheduler.deleteJob(jobKey);

            if (deleted) {
                scheduledJob.setCompleted(true);
                scheduledJob.setUpdatedAt(LocalDateTime.now());
                if (scheduledJob.getJobData() == null) {
                    scheduledJob.setJobData(new HashMap<>());
                }
                scheduledJob.getJobData().put("cancellationReason", "Manually cancelled");
                scheduledJob.getJobData().put("cancelledAt", LocalDateTime.now().toString());
                scheduledJobRepository.save(scheduledJob);

                logger.info("🗑️ Successfully cancelled scheduled notification: {} (Job: {})", 
                           notificationId, scheduledJob.getJobKey());
                return true;
            } else {
                logger.warn("⚠️ Failed to delete Quartz job: {}", jobKey);
                return false;
            }

        } catch (SchedulerException e) {
            logger.error("❌ Error cancelling scheduled notification {}: {}", notificationId, e.getMessage(), e);
            return false;
        }
    }

    public void rescheduleNotification(Notification notification) {
        // Cancel existing schedule
        cancelScheduledNotification(notification.getId());
        
        // Create new schedule
        scheduleNotification(notification);
        
        logger.info("🔄 Rescheduled notification: {} for {}", 
                   notification.getTitle(), notification.getScheduledAt());
    }

    private ScheduledJob createScheduledJobRecord(Notification notification, String jobKey, String jobGroup) {
        ScheduledJob scheduledJob = new ScheduledJob();
        scheduledJob.setNotification(notification);
        scheduledJob.setJobKey(jobKey);
        scheduledJob.setJobGroup(jobGroup);
        scheduledJob.setScheduledTime(notification.getScheduledAt());
        scheduledJob.setRecurring(false);
        scheduledJob.setCompleted(false);
        scheduledJob.setCreatedAt(LocalDateTime.now());
        scheduledJob.setUpdatedAt(LocalDateTime.now());
        
        // Store job metadata
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("notificationId", notification.getId());
        jobData.put("channelType", notification.getChannelType().name());
        jobData.put("priority", notification.getPriority().name());
        jobData.put("title", notification.getTitle());
        jobData.put("scheduledBy", "system");
        jobData.put("createdAt", LocalDateTime.now().toString());
        scheduledJob.setJobData(jobData);
        
        return scheduledJob;
    }

    public boolean isJobScheduled(Long notificationId) {
        return scheduledJobRepository
                .findByNotificationIdAndIsCompleted(notificationId, false)
                .isPresent();
    }

    public void printSchedulerInfo() {
        try {
            logger.info("📊 Quartz Scheduler Info:");
            logger.info("   - Scheduler Name: {}", scheduler.getSchedulerName());
            logger.info("   - Instance ID: {}", scheduler.getSchedulerInstanceId());
            logger.info("   - Started: {}", scheduler.isStarted());
            logger.info("   - In Standby: {}", scheduler.isInStandbyMode());
            logger.info("   - Shutdown: {}", scheduler.isShutdown());
        } catch (SchedulerException e) {
            logger.error("❌ Error getting scheduler info: {}", e.getMessage());
        }
    }
}