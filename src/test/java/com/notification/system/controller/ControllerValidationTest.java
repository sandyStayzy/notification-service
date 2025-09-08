package com.notification.system.controller;

import com.notification.system.model.dto.request.NotificationRequest;
import com.notification.system.model.dto.request.BatchNotificationRequest;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ControllerValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidNotificationRequest() {
        NotificationRequest request = new NotificationRequest(1L, "Test Title", "Test Content", ChannelType.EMAIL);
        request.setPriority(Priority.HIGH);
        request.setMetadata(Map.of("key", "value"));

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNotificationRequestWithNullUserId() {
        NotificationRequest request = new NotificationRequest(null, "Test Title", "Test Content", ChannelType.EMAIL);

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("must not be null"));
    }

    @Test
    void testNotificationRequestWithBlankTitle() {
        NotificationRequest request = new NotificationRequest(1L, "", "Test Content", ChannelType.EMAIL);

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("must not be blank"));
    }

    @Test
    void testNotificationRequestWithBlankContent() {
        NotificationRequest request = new NotificationRequest(1L, "Test Title", "", ChannelType.EMAIL);

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("must not be blank"));
    }

    @Test
    void testNotificationRequestWithNullChannelType() {
        NotificationRequest request = new NotificationRequest(1L, "Test Title", "Test Content", null);

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("must not be null"));
    }

    @Test
    void testNotificationRequestWithScheduledTime() {
        NotificationRequest request = new NotificationRequest(1L, "Test Title", "Test Content", ChannelType.EMAIL);
        request.setScheduledAt(LocalDateTime.now().plusHours(1));

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNotificationRequestDefaultPriority() {
        NotificationRequest request = new NotificationRequest();
        assertEquals(Priority.MEDIUM, request.getPriority());
    }

    @Test
    void testNotificationRequestAllFields() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("campaign", "welcome");
        metadata.put("version", "v1.0");

        NotificationRequest request = new NotificationRequest(1L, "Test Title", "Test Content", ChannelType.SMS);
        request.setPriority(Priority.LOW);
        request.setScheduledAt(LocalDateTime.now().plusDays(1));
        request.setMetadata(metadata);

        assertEquals(1L, request.getUserId());
        assertEquals("Test Title", request.getTitle());
        assertEquals("Test Content", request.getContent());
        assertEquals(ChannelType.SMS, request.getChannelType());
        assertEquals(Priority.LOW, request.getPriority());
        assertNotNull(request.getScheduledAt());
        assertEquals(metadata, request.getMetadata());

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidBatchNotificationRequest() {
        BatchNotificationRequest request = new BatchNotificationRequest();
        request.setTitle("Batch Title");
        request.setContent("Batch Content");
        request.setChannelType(ChannelType.PUSH);
        request.setPriority(Priority.HIGH);
        request.setUserIds(Arrays.asList(1L, 2L, 3L));

        BatchNotificationRequest.BatchSettings settings = new BatchNotificationRequest.BatchSettings();
        settings.setBatchSize(10);
        settings.setParallelProcessing(true);
        settings.setContinueOnError(false);
        settings.setDelayBetweenBatches(100L);
        request.setBatchSettings(settings);

        assertEquals("Batch Title", request.getTitle());
        assertEquals("Batch Content", request.getContent());
        assertEquals(ChannelType.PUSH, request.getChannelType());
        assertEquals(Priority.HIGH, request.getPriority());
        assertEquals(3, request.getUserIds().size());
        assertNotNull(request.getBatchSettings());
        assertEquals(10, request.getBatchSettings().getBatchSize());
        assertTrue(request.getBatchSettings().getParallelProcessing());
        assertFalse(request.getBatchSettings().getContinueOnError());
        assertEquals(100L, request.getBatchSettings().getDelayBetweenBatches());
    }

    @Test
    void testBatchSettingsDefaults() {
        BatchNotificationRequest.BatchSettings settings = new BatchNotificationRequest.BatchSettings();

        assertEquals(50, settings.getBatchSize());
        assertTrue(settings.getParallelProcessing());
        assertTrue(settings.getContinueOnError());
        assertEquals(1000L, settings.getDelayBetweenBatches());
    }

    @Test
    void testBatchNotificationRequestWithScheduling() {
        BatchNotificationRequest request = new BatchNotificationRequest();
        request.setTitle("Scheduled Batch");
        request.setContent("Scheduled Content");
        request.setChannelType(ChannelType.EMAIL);
        request.setUserIds(Arrays.asList(1L, 2L));
        request.setScheduledAt(LocalDateTime.now().plusHours(2));

        assertNotNull(request.getScheduledAt());
        assertTrue(request.getScheduledAt().isAfter(LocalDateTime.now()));
    }
}