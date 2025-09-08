package com.notification.system.model.dto.response;

import com.notification.system.model.enums.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationResultTest {

    private NotificationResult notificationResult;

    @BeforeEach
    void setUp() {
        notificationResult = new NotificationResult(true, NotificationStatus.SENT, "Successfully sent");
    }

    @Test
    void testNotificationResultCreation() {
        assertNotNull(notificationResult);
        assertTrue(notificationResult.isSuccess());
        assertEquals(NotificationStatus.SENT, notificationResult.getStatus());
        assertEquals("Successfully sent", notificationResult.getMessage());
    }

    @Test
    void testNotificationResultDefaultConstructor() {
        NotificationResult emptyResult = new NotificationResult();
        assertNotNull(emptyResult);
        assertFalse(emptyResult.isSuccess());
        assertNull(emptyResult.getStatus());
        assertNull(emptyResult.getMessage());
    }

    @Test
    void testSuccessfulResult() {
        NotificationResult successResult = new NotificationResult(true, NotificationStatus.SENT, "Email sent successfully");
        
        assertTrue(successResult.isSuccess());
        assertEquals(NotificationStatus.SENT, successResult.getStatus());
        assertEquals("Email sent successfully", successResult.getMessage());
    }

    @Test
    void testFailedResult() {
        NotificationResult failedResult = new NotificationResult(false, NotificationStatus.FAILED, "Failed to send email");
        
        assertFalse(failedResult.isSuccess());
        assertEquals(NotificationStatus.FAILED, failedResult.getStatus());
        assertEquals("Failed to send email", failedResult.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        notificationResult.setSuccess(false);
        notificationResult.setStatus(NotificationStatus.FAILED);
        notificationResult.setMessage("Updated message");
        
        assertFalse(notificationResult.isSuccess());
        assertEquals(NotificationStatus.FAILED, notificationResult.getStatus());
        assertEquals("Updated message", notificationResult.getMessage());
    }

    @Test
    void testPendingResult() {
        NotificationResult pendingResult = new NotificationResult(true, NotificationStatus.PENDING, "Notification queued for processing");
        
        assertTrue(pendingResult.isSuccess());
        assertEquals(NotificationStatus.PENDING, pendingResult.getStatus());
        assertEquals("Notification queued for processing", pendingResult.getMessage());
    }

    @Test
    void testScheduledResult() {
        NotificationResult scheduledResult = new NotificationResult(true, NotificationStatus.SCHEDULED, "Notification scheduled for later");
        
        assertTrue(scheduledResult.isSuccess());
        assertEquals(NotificationStatus.SCHEDULED, scheduledResult.getStatus());
        assertEquals("Notification scheduled for later", scheduledResult.getMessage());
    }

    @Test
    void testNullMessage() {
        NotificationResult resultWithNullMessage = new NotificationResult(true, NotificationStatus.SENT, null);
        
        assertTrue(resultWithNullMessage.isSuccess());
        assertEquals(NotificationStatus.SENT, resultWithNullMessage.getStatus());
        assertNull(resultWithNullMessage.getMessage());
    }

    @Test
    void testEmptyMessage() {
        NotificationResult resultWithEmptyMessage = new NotificationResult(true, NotificationStatus.SENT, "");
        
        assertTrue(resultWithEmptyMessage.isSuccess());
        assertEquals(NotificationStatus.SENT, resultWithEmptyMessage.getStatus());
        assertEquals("", resultWithEmptyMessage.getMessage());
    }
}