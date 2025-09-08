package com.notification.system.controller;

import com.notification.system.model.dto.request.BatchNotificationRequest;
import com.notification.system.model.dto.request.NotificationRequest;
import com.notification.system.model.dto.response.BatchNotificationResponse;
import com.notification.system.model.dto.response.NotificationResponse;
import com.notification.system.service.notification.BatchNotificationService;
import com.notification.system.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private BatchNotificationService batchNotificationService;
    
    @PostMapping
    @Operation(summary = "Send a notification", description = "Send an immediate or scheduled notification")
    public ResponseEntity<NotificationResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by its ID")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        return notificationService.getNotification(id)
                .map(notification -> ResponseEntity.ok(notification))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications", description = "Retrieve paginated notifications for a specific user")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }
    
    @PostMapping("/batch")
    @Operation(summary = "Send batch notifications", 
               description = "Send notifications to multiple users with configurable batch processing settings")
    public ResponseEntity<BatchNotificationResponse> sendBatchNotification(
            @Valid @RequestBody BatchNotificationRequest request) {
        
        BatchNotificationResponse response = batchNotificationService.processBatchNotification(request);
        
        HttpStatus status = switch (response.getStatus()) {
            case COMPLETED -> HttpStatus.OK;
            case PARTIALLY_FAILED -> HttpStatus.MULTI_STATUS;
            case FAILED, CANCELLED -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.ACCEPTED;
        };
        
        return ResponseEntity.status(status).body(response);
    }
}