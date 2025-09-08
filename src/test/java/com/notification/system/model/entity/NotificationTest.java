package com.notification.system.model.entity;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.model.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    private User testUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        
        notification = new Notification(testUser, "Test Title", "Test Content", ChannelType.EMAIL, Priority.HIGH);
    }

    @Test
    void testNotificationCreation() {
        assertNotNull(notification);
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Content", notification.getContent());
        assertEquals(ChannelType.EMAIL, notification.getChannelType());
        assertEquals(Priority.HIGH, notification.getPriority());
        assertEquals(testUser, notification.getUser());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertEquals(0, notification.getRetryCount());
        assertNull(notification.getCreatedAt());
        assertNull(notification.getUpdatedAt());
    }

    @Test
    void testNotificationDefaultConstructor() {
        Notification emptyNotification = new Notification();
        assertNotNull(emptyNotification);
        assertNull(emptyNotification.getTitle());
        assertNull(emptyNotification.getContent());
        assertNull(emptyNotification.getChannelType());
        assertEquals(Priority.MEDIUM, emptyNotification.getPriority());
        assertNull(emptyNotification.getUser());
        assertEquals(0, emptyNotification.getRetryCount());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = now.plusHours(1);
        LocalDateTime sent = now.plusMinutes(30);
        LocalDateTime nextRetry = now.plusMinutes(5);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("campaign", "welcome");
        metadata.put("version", "v1.0");
        
        notification.setId(100L);
        notification.setStatus(NotificationStatus.SENT);
        notification.setScheduledAt(scheduled);
        notification.setSentAt(sent);
        notification.setRetryCount(2);
        notification.setNextRetryAt(nextRetry);
        notification.setErrorMessage("Test error");
        notification.setMetadata(metadata);
        notification.setUpdatedAt(now);
        
        assertEquals(100L, notification.getId());
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertEquals(scheduled, notification.getScheduledAt());
        assertEquals(sent, notification.getSentAt());
        assertEquals(2, notification.getRetryCount());
        assertEquals(nextRetry, notification.getNextRetryAt());
        assertEquals("Test error", notification.getErrorMessage());
        assertEquals(metadata, notification.getMetadata());
        assertEquals(now, notification.getUpdatedAt());
    }

    @Test
    void testMetadataHandling() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        metadata.put("key3", true);
        
        notification.setMetadata(metadata);
        
        assertEquals(metadata, notification.getMetadata());
        assertEquals("value1", notification.getMetadata().get("key1"));
        assertEquals(123, notification.getMetadata().get("key2"));
        assertEquals(true, notification.getMetadata().get("key3"));
    }

    @Test
    void testRetryLogic() {
        assertEquals(0, notification.getRetryCount());
        
        notification.setRetryCount(1);
        assertEquals(1, notification.getRetryCount());
        
        notification.setRetryCount(notification.getRetryCount() + 1);
        assertEquals(2, notification.getRetryCount());
    }

}