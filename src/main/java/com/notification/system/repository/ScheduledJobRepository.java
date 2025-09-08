package com.notification.system.repository;

import com.notification.system.model.entity.ScheduledJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    
    Optional<ScheduledJob> findByJobKeyAndJobGroup(String jobKey, String jobGroup);
    
    List<ScheduledJob> findByScheduledTimeBeforeAndIsCompletedFalse(LocalDateTime currentTime);
    
    List<ScheduledJob> findByIsRecurringTrueAndIsCompletedFalse();
    
    Optional<ScheduledJob> findByNotificationIdAndIsCompleted(Long notificationId, Boolean isCompleted);
    
    void deleteByJobKeyAndJobGroup(String jobKey, String jobGroup);
}