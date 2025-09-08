package com.notification.system.model.dto.event;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventTest {

    private NotificationEvent notificationEvent;

    @BeforeEach
    void setUp() {
        notificationEvent = new NotificationEvent(1L, 100L, "Test Title", "Test Content", ChannelType.EMAIL, Priority.HIGH);
    }

    @Test
    void testNotificationEventCreation() {
        assertNotNull(notificationEvent);
        assertEquals(1L, notificationEvent.getNotificationId());
        assertEquals(100L, notificationEvent.getUserId());
        assertEquals("Test Title", notificationEvent.getTitle());
        assertEquals("Test Content", notificationEvent.getContent());
        assertEquals(ChannelType.EMAIL, notificationEvent.getChannelType());
        assertEquals(Priority.HIGH, notificationEvent.getPriority());
        assertEquals("NOTIFICATION_CREATED", notificationEvent.getEventType());
        assertEquals(0, notificationEvent.getRetryCount());
        assertNotNull(notificationEvent.getCreatedAt());
        assertNotNull(notificationEvent.getEventId());
        assertTrue(notificationEvent.getEventId().startsWith("evt_"));
    }

    @Test
    void testNotificationEventDefaultConstructor() {
        NotificationEvent emptyEvent = new NotificationEvent();
        assertNotNull(emptyEvent);
        assertNull(emptyEvent.getNotificationId());
        assertNull(emptyEvent.getUserId());
        assertNull(emptyEvent.getTitle());
        assertNull(emptyEvent.getContent());
        assertNull(emptyEvent.getChannelType());
        assertNull(emptyEvent.getPriority());
        assertNull(emptyEvent.getEventType());
        assertEquals(0, emptyEvent.getRetryCount());
        assertNotNull(emptyEvent.getCreatedAt());
        assertNull(emptyEvent.getEventId());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        LocalDateTime createdTime = LocalDateTime.now();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("campaign", "welcome");
        
        notificationEvent.setEventId("custom_event_id");
        notificationEvent.setNotificationId(2L);
        notificationEvent.setUserId(200L);
        notificationEvent.setTitle("Updated Title");
        notificationEvent.setContent("Updated Content");
        notificationEvent.setChannelType(ChannelType.SMS);
        notificationEvent.setPriority(Priority.HIGH);
        notificationEvent.setMetadata(metadata);
        notificationEvent.setScheduledAt(scheduledTime);
        notificationEvent.setCreatedAt(createdTime);
        notificationEvent.setEventType("CUSTOM_EVENT");
        notificationEvent.setRetryCount(2);
        
        assertEquals("custom_event_id", notificationEvent.getEventId());
        assertEquals(2L, notificationEvent.getNotificationId());
        assertEquals(200L, notificationEvent.getUserId());
        assertEquals("Updated Title", notificationEvent.getTitle());
        assertEquals("Updated Content", notificationEvent.getContent());
        assertEquals(ChannelType.SMS, notificationEvent.getChannelType());
        assertEquals(Priority.HIGH, notificationEvent.getPriority());
        assertEquals(metadata, notificationEvent.getMetadata());
        assertEquals(scheduledTime, notificationEvent.getScheduledAt());
        assertEquals(createdTime, notificationEvent.getCreatedAt());
        assertEquals("CUSTOM_EVENT", notificationEvent.getEventType());
        assertEquals(2, notificationEvent.getRetryCount());
    }

    @Test
    void testEventIdGeneration() {
        String eventId = notificationEvent.getEventId();
        assertNotNull(eventId);
        assertTrue(eventId.startsWith("evt_"));
        assertTrue(eventId.contains("_1"));
    }

    @Test
    void testMetadataHandling() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", 123);
        metadata.put("key3", true);
        
        notificationEvent.setMetadata(metadata);
        
        assertEquals(metadata, notificationEvent.getMetadata());
        assertEquals("value1", notificationEvent.getMetadata().get("key1"));
        assertEquals(123, notificationEvent.getMetadata().get("key2"));
        assertEquals(true, notificationEvent.getMetadata().get("key3"));
    }

    @Test
    void testRetryCount() {
        assertEquals(0, notificationEvent.getRetryCount());
        
        notificationEvent.setRetryCount(3);
        assertEquals(3, notificationEvent.getRetryCount());
        
        notificationEvent.setRetryCount(0);
        assertEquals(0, notificationEvent.getRetryCount());
    }

    @Test
    void testScheduledAtHandling() {
        assertNull(notificationEvent.getScheduledAt());
        
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        notificationEvent.setScheduledAt(futureTime);
        
        assertEquals(futureTime, notificationEvent.getScheduledAt());
        
        notificationEvent.setScheduledAt(null);
        assertNull(notificationEvent.getScheduledAt());
    }

    @Test
    void testAllChannelTypes() {
        notificationEvent.setChannelType(ChannelType.EMAIL);
        assertEquals(ChannelType.EMAIL, notificationEvent.getChannelType());
        
        notificationEvent.setChannelType(ChannelType.SMS);
        assertEquals(ChannelType.SMS, notificationEvent.getChannelType());
        
        notificationEvent.setChannelType(ChannelType.PUSH);
        assertEquals(ChannelType.PUSH, notificationEvent.getChannelType());
    }

    @Test
    void testAllPriorities() {
        notificationEvent.setPriority(Priority.LOW);
        assertEquals(Priority.LOW, notificationEvent.getPriority());
        
        notificationEvent.setPriority(Priority.MEDIUM);
        assertEquals(Priority.MEDIUM, notificationEvent.getPriority());
        
        notificationEvent.setPriority(Priority.HIGH);
        assertEquals(Priority.HIGH, notificationEvent.getPriority());
        
        notificationEvent.setPriority(Priority.HIGH);
        assertEquals(Priority.HIGH, notificationEvent.getPriority());
    }

    @Test
    void testToString() {
        String toString = notificationEvent.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("NotificationEvent"));
        assertTrue(toString.contains("notificationId=1"));
        assertTrue(toString.contains("userId=100"));
        assertTrue(toString.contains("title='Test Title'"));
        assertTrue(toString.contains("channelType=EMAIL"));
        assertTrue(toString.contains("priority=HIGH"));
        assertTrue(toString.contains("eventType='NOTIFICATION_CREATED'"));
    }

    @Test
    void testCreatedAtInitialization() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        NotificationEvent newEvent = new NotificationEvent();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        
        assertTrue(newEvent.getCreatedAt().isAfter(before));
        assertTrue(newEvent.getCreatedAt().isBefore(after));
    }
}