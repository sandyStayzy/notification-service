package com.notification.system.model.dto.response;

import com.notification.system.model.enums.BatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "Batch notification response")
public class BatchNotificationResponse {

    @Schema(description = "Batch processing ID", example = "batch_12345")
    private String batchId;

    @Schema(description = "Total users targeted", example = "100")
    private Integer totalUsers;

    @Schema(description = "Successfully processed notifications", example = "95")
    private Integer successCount;

    @Schema(description = "Failed notifications", example = "5")  
    private Integer failureCount;

    @Schema(description = "Batch processing status", example = "COMPLETED")
    private BatchStatus status;

    @Schema(description = "Individual notification results")
    private List<NotificationResult> results;

    @Schema(description = "Processing start time")
    private LocalDateTime startedAt;

    @Schema(description = "Processing completion time")
    private LocalDateTime completedAt;

    @Schema(description = "Processing duration in milliseconds", example = "5420")
    private Long processingTimeMs;

    @Schema(description = "Batch processing statistics")
    private BatchStatistics statistics;

    @Schema(description = "Error details if any")
    private String errorMessage;

    public static class NotificationResult {
        private Long userId;
        private Long notificationId;
        private boolean success;
        private String message;
        private LocalDateTime processedAt;

        public NotificationResult(Long userId, Long notificationId, boolean success, String message) {
            this.userId = userId;
            this.notificationId = notificationId;
            this.success = success;
            this.message = message;
            this.processedAt = LocalDateTime.now();
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getNotificationId() { return notificationId; }
        public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }

    public static class BatchStatistics {
        private Integer totalBatches;
        private Integer processedBatches;
        private Double averageProcessingTimePerBatch;
        private Double successRate;
        private Map<String, Integer> errorBreakdown;

        // Getters and Setters
        public Integer getTotalBatches() { return totalBatches; }
        public void setTotalBatches(Integer totalBatches) { this.totalBatches = totalBatches; }

        public Integer getProcessedBatches() { return processedBatches; }
        public void setProcessedBatches(Integer processedBatches) { this.processedBatches = processedBatches; }

        public Double getAverageProcessingTimePerBatch() { return averageProcessingTimePerBatch; }
        public void setAverageProcessingTimePerBatch(Double averageProcessingTimePerBatch) { 
            this.averageProcessingTimePerBatch = averageProcessingTimePerBatch; 
        }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Map<String, Integer> getErrorBreakdown() { return errorBreakdown; }
        public void setErrorBreakdown(Map<String, Integer> errorBreakdown) { this.errorBreakdown = errorBreakdown; }
    }

    // Constructors
    public BatchNotificationResponse() {}

    public BatchNotificationResponse(String batchId, Integer totalUsers) {
        this.batchId = batchId;
        this.totalUsers = totalUsers;
        this.successCount = 0;
        this.failureCount = 0;
        this.status = BatchStatus.QUEUED;
        this.startedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public Integer getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }

    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

    public Integer getFailureCount() { return failureCount; }
    public void setFailureCount(Integer failureCount) { this.failureCount = failureCount; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public List<NotificationResult> getResults() { return results; }
    public void setResults(List<NotificationResult> results) { this.results = results; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public BatchStatistics getStatistics() { return statistics; }
    public void setStatistics(BatchStatistics statistics) { this.statistics = statistics; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public void setTotalRequested(Integer totalRequested) { this.totalUsers = totalRequested; }
    public void setSuccessfulCount(Integer successfulCount) { this.successCount = successfulCount; }
    public void setFailedCount(Integer failedCount) { this.failureCount = failedCount; }
    public void setProcessingTime(Long processingTime) { this.processingTimeMs = processingTime; }
    public void setCreatedAt(LocalDateTime createdAt) { this.startedAt = createdAt; }
}