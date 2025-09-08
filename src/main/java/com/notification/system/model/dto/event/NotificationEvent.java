package com.notification.system.model.dto.event;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.Priority;

import java.time.LocalDateTime;
import java.util.Map;

public class NotificationEvent {
    
    private String eventId;
    private Long notificationId;
    private Long userId;
    private String title;
    private String content;
    private ChannelType channelType;
    private Priority priority;
    private Map<String, Object> metadata;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private String eventType;
    private Integer retryCount;
    
    public NotificationEvent() {
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }
    
    public NotificationEvent(Long notificationId, Long userId, String title, String content,
                           ChannelType channelType, Priority priority) {
        this();
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.channelType = channelType;
        this.priority = priority;
        this.eventType = "NOTIFICATION_CREATED";
        this.eventId = "evt_" + System.currentTimeMillis() + "_" + notificationId;
    }
    
    // Getters and Setters
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public Long getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
    
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
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }
    
    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public Integer getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    
    @Override
    public String toString() {
        return "NotificationEvent{" +
                "eventId='" + eventId + '\'' +
                ", notificationId=" + notificationId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", channelType=" + channelType +
                ", priority=" + priority +
                ", eventType='" + eventType + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}