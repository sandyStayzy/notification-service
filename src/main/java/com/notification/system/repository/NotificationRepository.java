package com.notification.system.repository;

import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.User;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.model.enums.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUser(User user, Pageable pageable);
    
    Page<Notification> findByUserAndStatus(User user, NotificationStatus status, Pageable pageable);
    
    List<Notification> findByStatusAndScheduledAtLessThanEqual(NotificationStatus status, LocalDateTime scheduledAt);
    
    List<Notification> findByStatusAndRetryCountLessThanAndNextRetryAtLessThanEqual(
            NotificationStatus status, int maxRetryCount, LocalDateTime currentTime);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status ORDER BY n.priority ASC, n.createdAt ASC")
    List<Notification> findPendingNotificationsByPriorityAndTime(@Param("status") NotificationStatus status, Pageable pageable);
    
    long countByUserAndStatus(User user, NotificationStatus status);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status AND n.createdAt BETWEEN :startTime AND :endTime")
    long countByStatusAndTimeRange(@Param("status") NotificationStatus status, 
                                 @Param("startTime") LocalDateTime startTime, 
                                 @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.id = :id")
    Optional<Notification> findByIdWithUser(@Param("id") Long id);
}