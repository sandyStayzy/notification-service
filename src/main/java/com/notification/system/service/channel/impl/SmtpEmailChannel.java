package com.notification.system.service.channel.impl;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.service.channel.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "notification.channels.email.smtp.enabled", havingValue = "true")
@Order(1) // Higher priority than console email
public class SmtpEmailChannel implements NotificationChannel {

    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailChannel.class);

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public NotificationResult send(Notification notification) {
        try {
            if (notification.getUser().getEmail() == null || notification.getUser().getEmail().trim().isEmpty()) {
                String error = "Cannot send email notification to user " + notification.getUser().getUsername() + ": no email address";
                logger.warn(error);
                return NotificationResult.failure("No email address", error);
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getUser().getEmail());
            helper.setSubject(notification.getTitle());
            helper.setText(buildEmailContent(notification), true);
            helper.setFrom("noreply@notificationservice.com");

            // Simulate processing time
            Thread.sleep(300);
            
            mailSender.send(message);

            logEmailSent(notification);
            logger.info("SMTP email sent successfully to {}", notification.getUser().getEmail());
            
            return NotificationResult.success("SMTP email sent successfully to " + notification.getUser().getEmail());

        } catch (MessagingException e) {
            String error = "Failed to send SMTP email to " + notification.getUser().getEmail() + ": " + e.getMessage();
            logger.error(error);
            return NotificationResult.failure("SMTP email failed", error);
        } catch (Exception e) {
            String error = "Unexpected error sending SMTP email to " + notification.getUser().getEmail() + ": " + e.getMessage();
            logger.error(error);
            return NotificationResult.failure("SMTP email error", error);
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
        return "SMTP Email Channel";
    }

    private String buildEmailContent(Notification notification) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'><title>").append(notification.getTitle()).append("</title></head>");
        html.append("<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        
        html.append("<div style='background-color: #f8f9fa; padding: 30px; border-radius: 10px; margin-bottom: 20px;'>");
        html.append("<h1 style='color: #2c3e50; margin: 0 0 20px 0; font-size: 24px;'>").append(notification.getTitle()).append("</h1>");
        html.append("<div style='background-color: white; padding: 20px; border-radius: 8px; border-left: 4px solid #3498db;'>");
        html.append("<p style='margin: 0; font-size: 16px;'>").append(notification.getContent()).append("</p>");
        html.append("</div>");
        html.append("</div>");
        
        if (notification.getMetadata() != null && !notification.getMetadata().isEmpty()) {
            html.append("<div style='background-color: #f1f2f6; padding: 15px; border-radius: 5px; margin-bottom: 20px;'>");
            html.append("<h3 style='color: #2c3e50; margin: 0 0 10px 0; font-size: 16px;'>Additional Information</h3>");
            for (Map.Entry<String, Object> entry : notification.getMetadata().entrySet()) {
                html.append("<p style='margin: 5px 0; font-size: 14px;'>");
                html.append("<strong>").append(entry.getKey()).append(":</strong> ").append(entry.getValue());
                html.append("</p>");
            }
            html.append("</div>");
        }
        
        html.append("<div style='border-top: 1px solid #ddd; padding-top: 20px; text-align: center; color: #666; font-size: 12px;'>");
        html.append("<p style='margin: 5px 0;'>This email was sent by the Notification System</p>");
        html.append("<p style='margin: 5px 0;'>Priority: ").append(notification.getPriority()).append(" | ");
        html.append("Sent: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        html.append("</p>");
        html.append("</div>");
        
        html.append("</body></html>");
        return html.toString();
    }

    private void logEmailSent(Notification notification) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 SMTP EMAIL SENT");
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
        System.out.println("Email Format: Rich HTML with styling");
        System.out.println("=".repeat(60) + "\n");
    }
}