package com.notification.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.system.model.dto.request.BatchNotificationRequest;
import com.notification.system.model.dto.request.NotificationRequest;
import com.notification.system.model.dto.response.BatchNotificationResponse;
import com.notification.system.model.dto.response.NotificationResponse;
import com.notification.system.model.enums.BatchStatus;
import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.NotificationStatus;
import com.notification.system.model.enums.Priority;
import com.notification.system.service.notification.BatchNotificationService;
import com.notification.system.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private BatchNotificationService batchNotificationService;

    @Test
    void testSendNotification() throws Exception {
        NotificationRequest request = new NotificationRequest(1L, "Test Title", "Test Content", ChannelType.EMAIL);
        request.setPriority(Priority.HIGH);
        request.setMetadata(Map.of("key", "value"));

        NotificationResponse response = new NotificationResponse();
        response.setId(1L);
        response.setUserId(request.getUserId());
        response.setTitle(request.getTitle());
        response.setContent(request.getContent());
        response.setChannelType(request.getChannelType());
        response.setPriority(request.getPriority());
        response.setStatus(NotificationStatus.SENT);
        response.setCreatedAt(LocalDateTime.now());
        response.setMetadata(request.getMetadata());

        when(notificationService.sendNotification(any(NotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.title", is("Test Title")))
                .andExpect(jsonPath("$.content", is("Test Content")))
                .andExpect(jsonPath("$.channelType", is("EMAIL")))
                .andExpect(jsonPath("$.priority", is("HIGH")))
                .andExpect(jsonPath("$.status", is("SENT")))
                .andExpect(jsonPath("$.metadata.key", is("value")));
    }

    @Test
    void testSendNotificationWithValidationError() throws Exception {
        NotificationRequest request = new NotificationRequest(null, "", "", null);

        mockMvc.perform(post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetNotificationSuccess() throws Exception {
        NotificationResponse response = new NotificationResponse();
        response.setId(1L);
        response.setTitle("Test Notification");
        response.setContent("Test Content");
        response.setChannelType(ChannelType.EMAIL);

        when(notificationService.getNotification(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Notification")))
                .andExpect(jsonPath("$.content", is("Test Content")))
                .andExpect(jsonPath("$.channelType", is("EMAIL")));
    }

    @Test
    void testGetNotificationNotFound() throws Exception {
        when(notificationService.getNotification(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/notifications/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserNotifications() throws Exception {
        NotificationResponse notification = new NotificationResponse();
        notification.setId(1L);
        notification.setUserId(1L);
        notification.setTitle("User Notification");
        Page<NotificationResponse> page = new PageImpl<>(List.of(notification));

        when(notificationService.getUserNotifications(anyLong(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications/user/1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].userId", is(1)))
                .andExpect(jsonPath("$.content[0].title", is("User Notification")));
    }

    @Test
    void testGetUserNotificationsWithDefaultParams() throws Exception {
        Page<NotificationResponse> page = new PageImpl<>(List.of());
        when(notificationService.getUserNotifications(anyLong(), anyInt(), anyInt())).thenReturn(page);
        mockMvc.perform(get("/api/v1/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void testSendBatchNotificationCompleted() throws Exception {
        BatchNotificationRequest request = new BatchNotificationRequest();
        request.setTitle("Batch Title");
        request.setContent("Batch Content");
        request.setChannelType(ChannelType.EMAIL);
        request.setPriority(Priority.HIGH);
        request.setUserIds(Arrays.asList(1L, 2L, 3L));

        BatchNotificationResponse response = new BatchNotificationResponse();
        response.setBatchId("batch-123");
        response.setStatus(BatchStatus.COMPLETED);
        response.setTotalRequested(3);
        response.setSuccessfulCount(3);
        response.setFailedCount(0);

        when(batchNotificationService.processBatchNotification(any(BatchNotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.batchId", is("batch-123")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.totalRequested", is(3)))
                .andExpect(jsonPath("$.successfulCount", is(3)))
                .andExpect(jsonPath("$.failedCount", is(0)));
    }

    @Test
    void testSendBatchNotificationWithScheduling() throws Exception {
        BatchNotificationRequest request = new BatchNotificationRequest();
        request.setTitle("Scheduled Batch");
        request.setContent("Scheduled Content");
        request.setChannelType(ChannelType.PUSH);
        request.setPriority(Priority.MEDIUM);
        request.setUserIds(Arrays.asList(1L, 2L));
        request.setScheduledAt(LocalDateTime.now().plusHours(1));

        BatchNotificationResponse response = new BatchNotificationResponse();
        response.setStatus(BatchStatus.QUEUED);

        when(batchNotificationService.processBatchNotification(any(BatchNotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("QUEUED")));
    }

    @Test
    void testSendNotificationWithScheduledTime() throws Exception {
        NotificationRequest request = new NotificationRequest(1L, "Scheduled Title", "Scheduled Content", ChannelType.SMS);
        request.setScheduledAt(LocalDateTime.now().plusHours(2));
        request.setPriority(Priority.LOW);

        NotificationResponse response = new NotificationResponse();
        response.setTitle("Scheduled Title");
        response.setChannelType(ChannelType.SMS);
        response.setPriority(Priority.LOW);
        response.setStatus(NotificationStatus.SCHEDULED);

        when(notificationService.sendNotification(any(NotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Scheduled Title")))
                .andExpect(jsonPath("$.channelType", is("SMS")))
                .andExpect(jsonPath("$.priority", is("LOW")))
                .andExpect(jsonPath("$.status", is("SCHEDULED")));
    }
}
