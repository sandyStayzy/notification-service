package com.notification.system.service.channel;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.User;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.model.enums.Priority;
import com.notification.system.service.channel.impl.PushChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushChannelTest {
    
    private PushChannel pushChannel;
    private User testUser;
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        pushChannel = new PushChannel();
        
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        testNotification = new Notification(testUser, "Test Title", "Test Content", ChannelType.PUSH, Priority.HIGH);
        testNotification.setId(1L);
    }
    
    @Test
    void testSupports() {
        assertTrue(pushChannel.supports(ChannelType.PUSH));
        assertFalse(pushChannel.supports(ChannelType.EMAIL));
        assertFalse(pushChannel.supports(ChannelType.SMS));
    }
    
    @Test
    void testGetChannelType() {
        assertEquals(ChannelType.PUSH, pushChannel.getChannelType());
    }
    
    @Test
    void testGetChannelName() {
        assertEquals("Push Notification Channel", pushChannel.getChannelName());
    }
    
    @Test
    void testSendNotificationSuccess() {
        NotificationResult result = pushChannel.send(testNotification);
        
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertTrue(result.getMessage().contains("Push notification sent successfully"));
    }
    
    @Test
    void testSendNotificationWithMetadata() {
        testNotification.setMetadata(java.util.Map.of("campaign", "welcome", "version", "v1.0"));
        
        NotificationResult result = pushChannel.send(testNotification);
        
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.SENT, result.getStatus());
    }
}