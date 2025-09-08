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
@ConditionalOnProperty(name = "notification.channels.email.console.enabled", havingValue = "true", matchIfMissing = true)
@Order(2) // Lower priority than SMTP email
public class EmailChannel implements NotificationChannel {
    
    @Override
    public NotificationResult send(Notification notification) {
        try {
            // Simulate email sending processing time
            Thread.sleep(200);
            
            // Log detailed email information
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📧 EMAIL NOTIFICATION SENT");
            System.out.println("=".repeat(60));
            System.out.println("To: " + notification.getUser().getEmail());
            System.out.println("From: noreply@notificationservice.com");
            System.out.println("Subject: " + notification.getTitle());
            System.out.println("Priority: " + notification.getPriority());
            System.out.println("Sent At: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.out.println("\nContent:");
            System.out.println("-".repeat(40));
            System.out.println(notification.getContent());
            System.out.println("-".repeat(40));
            
            if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
                System.out.println("\nMetadata: " + notification.getMetadata());
            }
            
            System.out.println("=".repeat(60) + "\n");
            
            return NotificationResult.success("Email sent successfully to " + notification.getUser().getEmail());
            
        } catch (Exception e) {
            return NotificationResult.failure("Failed to send email", e.getMessage());
        }
    }
    
    @Override
    public boolean supports(ChannelType channelType) {
        return ChannelType.EMAIL == channelType;
    }
    
    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }
    
    @Override
    public String getChannelName() {
        return "Console Email Channel";
    }
}