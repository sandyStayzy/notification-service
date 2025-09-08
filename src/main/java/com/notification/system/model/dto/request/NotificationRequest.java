package com.notification.system.model.dto.request;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public class NotificationRequest {
    
    @NotNull
    private Long userId;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String content;
    
    @NotNull
    private ChannelType channelType;
    
    private Priority priority = Priority.MEDIUM;
    
    private LocalDateTime scheduledAt;
    
    private Map<String, Object> metadata;

    public NotificationRequest() {}

    public NotificationRequest(Long userId, String title, String content, ChannelType channelType) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.channelType = channelType;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}