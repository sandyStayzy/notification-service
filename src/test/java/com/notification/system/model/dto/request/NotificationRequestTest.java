package com.notification.system.model.dto.request;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestTest {

    private NotificationRequest notificationRequest;

    @BeforeEach
    void setUp() {
        notificationRequest = new NotificationRequest(1L, "Test Title", "Test Content", ChannelType.EMAIL);
    }

    @Test
    void testNotificationRequestCreation() {
        assertNotNull(notificationRequest);
        assertEquals(1L, notificationRequest.getUserId());
        assertEquals("Test Title", notificationRequest.getTitle());
        assertEquals("Test Content", notificationRequest.getContent());
        assertEquals(ChannelType.EMAIL, notificationRequest.getChannelType());
        assertEquals(Priority.MEDIUM, notificationRequest.getPriority());
    }

    @Test
    void testNotificationRequestDefaultConstructor() {
        NotificationRequest emptyRequest = new NotificationRequest();
        assertNotNull(emptyRequest);
        assertNull(emptyRequest.getUserId());
        assertNull(emptyRequest.getTitle());
        assertNull(emptyRequest.getContent());
        assertNull(emptyRequest.getChannelType());
        assertEquals(Priority.MEDIUM, emptyRequest.getPriority());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("campaign", "welcome");
        
        notificationRequest.setUserId(2L);
        notificationRequest.setTitle("Updated Title");
        notificationRequest.setContent("Updated Content");
        notificationRequest.setChannelType(ChannelType.SMS);
        notificationRequest.setPriority(Priority.HIGH);
        notificationRequest.setScheduledAt(scheduledTime);
        notificationRequest.setMetadata(metadata);
        
        assertEquals(2L, notificationRequest.getUserId());
        assertEquals("Updated Title", notificationRequest.getTitle());
        assertEquals("Updated Content", notificationRequest.getContent());
        assertEquals(ChannelType.SMS, notificationRequest.getChannelType());
        assertEquals(Priority.HIGH, notificationRequest.getPriority());
        assertEquals(scheduledTime, notificationRequest.getScheduledAt());
        assertEquals(metadata, notificationRequest.getMetadata());
    }

    @Test
    void testDefaultPriority() {
        NotificationRequest request = new NotificationRequest();
        assertEquals(Priority.MEDIUM, request.getPriority());
    }

    @Test
    void testAllChannelTypes() {
        notificationRequest.setChannelType(ChannelType.EMAIL);
        assertEquals(ChannelType.EMAIL, notificationRequest.getChannelType());
        
        notificationRequest.setChannelType(ChannelType.SMS);
        assertEquals(ChannelType.SMS, notificationRequest.getChannelType());
        
        notificationRequest.setChannelType(ChannelType.PUSH);
        assertEquals(ChannelType.PUSH, notificationRequest.getChannelType());
    }

    @Test
    void testAllPriorities() {
        notificationRequest.setPriority(Priority.LOW);
        assertEquals(Priority.LOW, notificationRequest.getPriority());
        
        notificationRequest.setPriority(Priority.MEDIUM);
        assertEquals(Priority.MEDIUM, notificationRequest.getPriority());
        
        notificationRequest.setPriority(Priority.HIGH);
        assertEquals(Priority.HIGH, notificationRequest.getPriority());
        
        notificationRequest.setPriority(Priority.HIGH);
        assertEquals(Priority.HIGH, notificationRequest.getPriority());
    }

    @Test
    void testMetadataHandling() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        metadata.put("key3", true);
        
        notificationRequest.setMetadata(metadata);
        
        assertEquals(metadata, notificationRequest.getMetadata());
        assertEquals("value1", notificationRequest.getMetadata().get("key1"));
        assertEquals(123, notificationRequest.getMetadata().get("key2"));
        assertEquals(true, notificationRequest.getMetadata().get("key3"));
    }

    @Test
    void testScheduledAtHandling() {
        assertNull(notificationRequest.getScheduledAt());
        
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        notificationRequest.setScheduledAt(futureTime);
        
        assertEquals(futureTime, notificationRequest.getScheduledAt());
        
        notificationRequest.setScheduledAt(null);
        assertNull(notificationRequest.getScheduledAt());
    }

    @Test
    void testImmediateNotification() {
        assertNull(notificationRequest.getScheduledAt());
    }

    @Test
    void testScheduledNotification() {
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(2);
        notificationRequest.setScheduledAt(scheduledTime);
        
        assertEquals(scheduledTime, notificationRequest.getScheduledAt());
    }
}