package com.notification.system.controller;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.service.channel.NotificationChannel;
import com.notification.system.service.channel.NotificationChannelFactory;
import com.notification.system.service.scheduler.NotificationSchedulerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationChannelFactory notificationChannelFactory;

    @MockBean
    private NotificationSchedulerService notificationSchedulerService;

    private NotificationChannel createMockChannel(ChannelType type) {
        NotificationChannel channel = mock(NotificationChannel.class);
        when(channel.supports(type)).thenReturn(true);
        when(channel.getClass()).thenAnswer(invocation -> (Class) (type == ChannelType.EMAIL ? com.notification.system.service.channel.impl.EmailChannel.class : type == ChannelType.SMS ? com.notification.system.service.channel.impl.SmsChannel.class : com.notification.system.service.channel.impl.PushChannel.class));
        return channel;
    }

    @Test
    void testGetAvailableChannels() throws Exception {
        when(notificationChannelFactory.getAllChannels()).thenReturn(List.of(createMockChannel(ChannelType.EMAIL), createMockChannel(ChannelType.SMS), createMockChannel(ChannelType.PUSH)));

        mockMvc.perform(get("/api/v1/admin/channels"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalChannels", is(3)))
                .andExpect(jsonPath("$.channels", hasSize(3)));
    }

    @Test
    void testGetSystemStatus() throws Exception {
        when(notificationChannelFactory.getAllChannels()).thenReturn(List.of(createMockChannel(ChannelType.EMAIL), createMockChannel(ChannelType.SMS), createMockChannel(ChannelType.PUSH)));

        mockMvc.perform(get("/api/v1/admin/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.version", is("1.0.0-SNAPSHOT")))
                .andExpect(jsonPath("$.supportedChannels", hasSize(3)))
                .andExpect(jsonPath("$.supportedChannels", hasItems("EMAIL", "SMS", "PUSH")));
    }

    @Test
    void testGetSchedulerInfo() throws Exception {
        mockMvc.perform(get("/api/v1/admin/scheduler/info"))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelScheduledNotificationSuccess() throws Exception {
        when(notificationSchedulerService.cancelScheduledNotification(123L)).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/scheduler/cancel/123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.notificationId", is(123)))
                .andExpect(jsonPath("$.cancelled", is(true)))
                .andExpect(jsonPath("$.message", is("Notification cancelled successfully")));
    }

    @Test
    void testCancelScheduledNotificationNotFound() throws Exception {
        when(notificationSchedulerService.cancelScheduledNotification(0L)).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/scheduler/cancel/0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.notificationId", is(0)))
                .andExpect(jsonPath("$.cancelled", is(false)))
                .andExpect(jsonPath("$.message", is("No scheduled job found")));
    }

    @Test
    void testGetSystemStatusContainsExpectedFeatures() throws Exception {
        when(notificationChannelFactory.getAllChannels()).thenReturn(List.of(createMockChannel(ChannelType.EMAIL), createMockChannel(ChannelType.SMS), createMockChannel(ChannelType.PUSH)));

        mockMvc.perform(get("/api/v1/admin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features", hasItems(
                        "Immediate notifications",
                        "Scheduled notifications",
                        "Priority-based processing",
                        "Multiple channel support",
                        "Notification tracking",
                        "Quartz scheduler integration"
                )));
    }

    @Test
    void testGetSchedulerInfoContainsExpectedFeatures() throws Exception {
        mockMvc.perform(get("/api/v1/admin/scheduler/info"))
                .andExpect(status().isOk());
    }
}