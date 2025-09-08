package com.notification.system.service.channel.impl;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.service.channel.NotificationChannel;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "notification.channels.sms.twilio.enabled", havingValue = "true")
@Order(1) // Higher priority than console SMS
public class TwilioSmsChannel implements NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsChannel.class);

    @Value("${notification.channels.sms.twilio.account-sid}")
    private String accountSid;

    @Value("${notification.channels.sms.twilio.auth-token}")
    private String authToken;

    @Value("${notification.channels.sms.twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
        logger.info("Twilio SMS channel initialized with account SID: {}...", accountSid.substring(0, 10));
    }

    @Override
    public NotificationResult send(Notification notification) {
        try {
            String phoneNumber = notification.getUser().getPhoneNumber();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                String error = "Cannot send SMS notification to user " + notification.getUser().getUsername() + ": no phone number";
                logger.warn(error);
                return NotificationResult.failure("No phone number", error);
            }

            // Clean and validate phone number
            phoneNumber = cleanPhoneNumber(phoneNumber);
            if (!isValidPhoneNumber(phoneNumber)) {
                String error = "Invalid phone number format for user " + notification.getUser().getUsername() + ": " + phoneNumber;
                logger.warn(error);
                return NotificationResult.failure("Invalid phone number", error);
            }

            String messageContent = buildSmsContent(notification);
            
            // Simulate processing time
            Thread.sleep(500);

            Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromNumber),
                messageContent
            ).create();

            logSmsSent(notification, phoneNumber, message);
            logger.info("Twilio SMS sent successfully to {} with SID: {}", phoneNumber, message.getSid());
            
            return NotificationResult.success("Twilio SMS sent successfully to " + phoneNumber + " (SID: " + message.getSid() + ")");

        } catch (com.twilio.exception.TwilioException e) {
            String error = "Twilio SMS failed: " + e.getMessage();
            logger.error("Failed to send SMS via Twilio: {}", e.getMessage());
            return NotificationResult.failure("Twilio SMS failed", error);
        } catch (Exception e) {
            String error = "Unexpected error sending SMS via Twilio: " + e.getMessage();
            logger.error("Unexpected error sending SMS via Twilio: {}", e.getMessage());
            return NotificationResult.failure("SMS error", error);
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
        return "Twilio SMS Channel";
    }

    private String buildSmsContent(Notification notification) {
        StringBuilder sms = new StringBuilder();
        
        // Add title with emoji if it fits
        if (notification.getTitle() != null && !notification.getTitle().isEmpty()) {
            sms.append("📱 ").append(notification.getTitle()).append("\n\n");
        }
        
        // Add main content
        sms.append(notification.getContent());
        
        // Add key metadata if space allows
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            sms.append("\n\n");
            int metadataCount = 0;
            for (Map.Entry<String, Object> entry : notification.getMetadata().entrySet()) {
                if (metadataCount >= 2) break; // Limit metadata in SMS
                if (sms.length() + entry.getKey().length() + entry.getValue().toString().length() + 5 < 140) {
                    sms.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                    metadataCount++;
                }
            }
        }
        
        // Add priority if high
        if (notification.getPriority().name().equals("HIGH")) {
            sms.append("\n⚠️ URGENT");
        }
        
        // Truncate if too long (SMS limit is typically 160 characters)
        String content = sms.toString();
        if (content.length() > 155) {
            content = content.substring(0, 152) + "...";
        }
        
        return content;
    }

    private String cleanPhoneNumber(String phoneNumber) {
        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");
        
        // If no country code, assume US (+1)
        if (!cleaned.startsWith("+")) {
            if (cleaned.length() == 10) {
                cleaned = "+1" + cleaned;
            } else if (cleaned.length() == 11 && cleaned.startsWith("1")) {
                cleaned = "+" + cleaned;
            }
        }
        
        return cleaned;
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Basic validation for international phone numbers
        return phoneNumber.matches("^\\+[1-9]\\d{6,14}$");
    }

    private void logSmsSent(Notification notification, String phoneNumber, Message message) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📱 TWILIO SMS SENT");
        System.out.println("=".repeat(60));
        System.out.println("To: " + phoneNumber);
        System.out.println("From: " + fromNumber);
        System.out.println("Priority: " + notification.getPriority());
        System.out.println("Sent At: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println("Message SID: " + message.getSid());
        System.out.println("Status: " + message.getStatus());
        
        String content = buildSmsContent(notification);
        System.out.println("\nMessage:");
        System.out.println("-".repeat(40));
        System.out.println(content);
        System.out.println("-".repeat(40));
        
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            System.out.println("\nOriginal Metadata: " + notification.getMetadata());
        }
        System.out.println("Character Count: " + content.length());
        System.out.println("Service: Twilio");
        System.out.println("=".repeat(60) + "\n");
    }
}