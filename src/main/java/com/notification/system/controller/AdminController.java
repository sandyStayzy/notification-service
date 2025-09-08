package com.notification.system.controller;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.service.channel.NotificationChannel;
import com.notification.system.service.channel.NotificationChannelFactory;
import com.notification.system.service.scheduler.NotificationSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Administrative APIs")
public class AdminController {
    
    @Autowired
    private NotificationChannelFactory channelFactory;
    
    @Autowired
    private NotificationSchedulerService schedulerService;
    
    @GetMapping("/channels")
    @Operation(summary = "Get available channels", description = "Retrieve all available notification channels")
    public ResponseEntity<Map<String, Object>> getAvailableChannels() {
        List<NotificationChannel> channels = channelFactory.getAllChannels();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalChannels", channels.size());
        response.put("channels", channels.stream()
                .map(channel -> {
                    Map<String, Object> channelInfo = new HashMap<>();
                    channelInfo.put("type", channel.getChannelType());
                    channelInfo.put("name", channel.getChannelName());
                    channelInfo.put("supported", true);
                    return channelInfo;
                })
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get system status", description = "Retrieve system health and status information")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("version", "1.0.0-SNAPSHOT");
        status.put("supportedChannels", List.of(
                ChannelType.EMAIL.name(),
                ChannelType.SMS.name(),
                ChannelType.PUSH.name()
        ));
        status.put("features", List.of(
                "Immediate notifications",
                "Scheduled notifications", 
                "Priority-based processing",
                "Multiple channel support",
                "Notification tracking",
                "Quartz scheduler integration"
        ));
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/scheduler/info")
    @Operation(summary = "Get scheduler info", description = "Retrieve Quartz scheduler information")
    public ResponseEntity<Map<String, Object>> getSchedulerInfo() {
        schedulerService.printSchedulerInfo();
        
        Map<String, Object> info = new HashMap<>();
        info.put("schedulerType", "Quartz");
        info.put("status", "active");
        info.put("features", List.of(
                "Job scheduling",
                "Job cancellation",
                "Job rescheduling",
                "Persistent jobs",
                "Misfire handling"
        ));
        
        return ResponseEntity.ok(info);
    }
    
    @PostMapping("/scheduler/cancel/{notificationId}")
    @Operation(summary = "Cancel scheduled notification", description = "Cancel a scheduled notification job")
    public ResponseEntity<Map<String, Object>> cancelScheduledNotification(@PathVariable Long notificationId) {
        boolean cancelled = schedulerService.cancelScheduledNotification(notificationId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("notificationId", notificationId);
        response.put("cancelled", cancelled);
        response.put("message", cancelled ? "Notification cancelled successfully" : "No scheduled job found");
        
        return ResponseEntity.ok(response);
    }
}