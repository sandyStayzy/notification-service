package com.notification.system.model.dto.request;

import com.notification.system.model.enums.ChannelType;
import com.notification.system.model.enums.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Batch notification request")
public class BatchNotificationRequest {

    @NotEmpty(message = "User IDs cannot be empty")
    @Schema(description = "List of user IDs to send notification to", example = "[1, 2, 3]")
    private List<Long> userIds;

    @NotBlank(message = "Title cannot be blank")
    @Schema(description = "Notification title", example = "System Maintenance Notice")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    @Schema(description = "Notification content", example = "System will be under maintenance from 2-4 AM EST")
    private String content;

    @NotNull(message = "Channel type cannot be null")
    @Schema(description = "Notification channel type", example = "EMAIL")
    private ChannelType channelType;

    @NotNull(message = "Priority cannot be null")
    @Schema(description = "Notification priority", example = "HIGH")
    private Priority priority;

    @Schema(description = "Scheduled delivery time (optional)", example = "2024-12-25T10:00:00")
    private LocalDateTime scheduledAt;

    @Schema(description = "Additional metadata (optional)")
    private Map<String, Object> metadata;

    @Schema(description = "Batch processing settings")
    private BatchSettings batchSettings;

    public static class BatchSettings {
        @Schema(description = "Maximum batch size for processing", example = "50")
        private Integer batchSize = 50;

        @Schema(description = "Delay between batches in milliseconds", example = "1000")
        private Long delayBetweenBatches = 1000L;

        @Schema(description = "Enable parallel processing", example = "true")
        private Boolean parallelProcessing = true;

        @Schema(description = "Continue on error", example = "true")
        private Boolean continueOnError = true;

        // Getters and Setters
        public Integer getBatchSize() { return batchSize; }
        public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }

        public Long getDelayBetweenBatches() { return delayBetweenBatches; }
        public void setDelayBetweenBatches(Long delayBetweenBatches) { this.delayBetweenBatches = delayBetweenBatches; }

        public Boolean getParallelProcessing() { return parallelProcessing; }
        public void setParallelProcessing(Boolean parallelProcessing) { this.parallelProcessing = parallelProcessing; }

        public Boolean getContinueOnError() { return continueOnError; }
        public void setContinueOnError(Boolean continueOnError) { this.continueOnError = continueOnError; }
    }

    // Constructors
    public BatchNotificationRequest() {}

    public BatchNotificationRequest(List<Long> userIds, String title, String content, 
                                   ChannelType channelType, Priority priority) {
        this.userIds = userIds;
        this.title = title;
        this.content = content;
        this.channelType = channelType;
        this.priority = priority;
        this.batchSettings = new BatchSettings();
    }

    // Getters and Setters
    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ChannelType getChannelType() { return channelType; }
    public void setChannelType(ChannelType channelType) { this.channelType = channelType; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public BatchSettings getBatchSettings() { 
        return batchSettings != null ? batchSettings : new BatchSettings(); 
    }
    public void setBatchSettings(BatchSettings batchSettings) { this.batchSettings = batchSettings; }
}