package com.notification.system.service.channel;

import com.notification.system.model.dto.response.NotificationResult;
import com.notification.system.model.entity.Notification;
import com.notification.system.model.entity.User;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.model.enums.Priority;
import com.notification.system.service.channel.impl.EmailChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailChannelTest {
    
    private EmailChannel emailChannel;
    private User testUser;
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        emailChannel = new EmailChannel();
        
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        testNotification = new Notification(testUser, "Test Title", "Test Content", ChannelType.EMAIL, Priority.HIGH);
        testNotification.setId(1L);
    }
    
    @Test
    void testSupports() {
        assertTrue(emailChannel.supports(ChannelType.EMAIL));
        assertFalse(emailChannel.supports(ChannelType.SMS));
        assertFalse(emailChannel.supports(ChannelType.PUSH));
    }
    
    @Test
    void testGetChannelType() {
        assertEquals(ChannelType.EMAIL, emailChannel.getChannelType());
    }
    
    @Test
    void testGetChannelName() {
        assertEquals("Console Email Channel", emailChannel.getChannelName());
    }
    
    @Test
    void testSendNotificationSuccess() {
        NotificationResult result = emailChannel.send(testNotification);
        
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.SENT, result.getStatus());
        assertTrue(result.getMessage().contains("Email sent successfully"));
        assertTrue(result.getMessage().contains(testUser.getEmail()));
    }
    
    @Test
    void testSendNotificationWithMetadata() {
        testNotification.setMetadata(java.util.Map.of("campaign", "welcome", "version", "v1.0"));
        
        NotificationResult result = emailChannel.send(testNotification);
        
        assertTrue(result.isSuccess());
        assertEquals(NotificationStatus.SENT, result.getStatus());
    }
}