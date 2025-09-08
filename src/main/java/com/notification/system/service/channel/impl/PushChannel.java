package com.notification.system.service.channel.impl;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.service.channel.NotificationChannel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class PushChannel implements NotificationChannel {
    
    @Override
    public NotificationResult send(Notification notification) {
        try {
            // Simulate push notification processing time
            Thread.sleep(100);
            
            // Generate a mock device token
            String deviceToken = "device_" + UUID.randomUUID().toString().substring(0, 8);
            
            // Log detailed push notification information
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📲 PUSH NOTIFICATION SENT");
            System.out.println("=".repeat(60));
            System.out.println("User: " + notification.getUser().getUsername());
            System.out.println("Device Token: " + deviceToken);
            System.out.println("Title: " + notification.getTitle());
            System.out.println("Priority: " + notification.getPriority());
            System.out.println("Sent At: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.out.println("\nPayload:");
            System.out.println("-".repeat(40));
            System.out.println("{\n  \"title\": \"" + notification.getTitle() + "\",");
            System.out.println("  \"body\": \"" + notification.getContent() + "\",");
            System.out.println("  \"priority\": \"" + notification.getPriority().toString().toLowerCase() + "\",");
            System.out.println("  \"data\": " + (notification.getMetadata() != null ? notification.getMetadata() : "{}"));
            System.out.println("}");
            System.out.println("-".repeat(40));
            System.out.println("=".repeat(60) + "\n");
            
            return NotificationResult.success("Push notification sent successfully to device " + deviceToken);
            
        } catch (Exception e) {
            return NotificationResult.failure("Failed to send push notification", e.getMessage());
        }
    }
    
    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.PUSH == channelType;
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUSH;
    }
    
    @Override
    public String getChannelName() {
        return "Push Notification Channel";
    }
}