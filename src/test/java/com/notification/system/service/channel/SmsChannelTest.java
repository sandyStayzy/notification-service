package com.notification.system.service.channel;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.User;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.model.enums.Priority;
import com.notification.system.service.channel.impl.SmsChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmsChannelTest {
    
    private SmsChannel smsChannel;
    private User testUser;
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        smsChannel = new SmsChannel();
        
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        testUser.setPhoneNumber("+1234567890");
        
        testNotification = new Notification(testUser, "Test Title", "Test Content", ChannelType.SMS, Priority.HIGH);
        testNotification.setId(1L);
    }
    
    @Test
    void testSupports() {
        assertTrue(smsChannel.supports(ChannelType.SMS));
        assertFalse(smsChannel.supports(ChannelType.EMAIL));
        assertFalse(smsChannel.supports(ChannelType.PUSH));
    }
    
    @Test
    void testSendNotificationSuccess() {
        NotificationResult result = smsChannel.send(testNotification);
        
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertTrue(result.getMessage().contains("SMS sent successfully"));
        assertTrue(result.getMessage().contains(testUser.getPhoneNumber()));
    }
    
    @Test
    void testSendNotificationWithoutPhoneNumber() {
        testUser.setPhoneNumber(null);
        
        NotificationResult result = smsChannel.send(testNotification);
        
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.FAILED, result.getStatus());
        assertEquals("SMS failed", result.getMessage());
        assertEquals("User phone number not provided", result.getErrorDetails());
    }
    
    @Test
    void testSendNotificationWithEmptyPhoneNumber() {
        testUser.setPhoneNumber("   ");
        
        NotificationResult result = smsChannel.send(testNotification);
        
        assertFalse(result.isSuccess());
        assertEquals(NotificationStatus.FAILED, result.getStatus());
        assertEquals("SMS failed", result.getMessage());
        assertEquals("User phone number not provided", result.getErrorDetails());
    }
}