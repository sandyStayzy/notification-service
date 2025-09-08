package com.notification.system.service.channel.impl;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.service.channel.NotificationChannel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(name = "notification.channels.sms.console.enabled", havingValue = "true", matchIfMissing = true)
@Order(2) // Lower priority than Twilio SMS
public class SmsChannel implements NotificationChannel {
    
    @Override
    public NotificationResult send(Notification notification) {
        try {
            // Check if user has phone number
            if (notification.getUser().getPhoneNumber() == null || 
                notification.getUser().getPhoneNumber().trim().isEmpty()) {
                return NotificationResult.failure("SMS failed", "User phone number not provided");
            }
            
            // Simulate SMS sending processing time
            Thread.sleep(150);
            
            // Log detailed SMS information
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📱 SMS NOTIFICATION SENT");
            System.out.println("=".repeat(60));
            System.out.println("To: " + notification.getUser().getPhoneNumber());
            System.out.println("From: +1-555-NOTIFY");
            System.out.println("Priority: " + notification.getPriority());
            System.out.println("Sent At: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.out.println("\nMessage:");
            System.out.println("-".repeat(40));
            System.out.println(notification.getTitle());
            System.out.println(notification.getContent());
            System.out.println("-".repeat(40));
            
            if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
                System.out.println("\nMetadata: " + notification.getMetadata());
            }
            
            System.out.println("Character Count: " + (notification.getTitle().length() + notification.getContent().length()));
            System.out.println("=".repeat(60) + "\n");
            
            return NotificationResult.success("SMS sent successfully to " + notification.getUser().getPhoneNumber());
            
        } catch (Exception e) {
            return NotificationResult.failure("Failed to send SMS", e.getMessage());
        }
    }
    
    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.SMS == channelType;
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }
    
    @Override
    public String getChannelName() {
        return "Console SMS Channel";
    }
}