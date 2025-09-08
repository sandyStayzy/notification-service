package com.notification.system.model.dto.response;

import com.notification.system.model.enums.NotificationStatus;

public class NotificationResult {
    
    private boolean success;
    private NotificationStatus status;
    private String message;
    private String errorDetails;

    public NotificationResult() {}

    public NotificationResult(boolean success, NotificationStatus status, String message) {
        this.success = success;
        this.status = status;
        this.message = message;
    }

    public static NotificationResult success(String message) {
        return new NotificationResult(true, NotificationStatus.SENT, message);
    }
    
    public static NotificationResult failure(String message, String errorDetails) {
        NotificationResult result = new NotificationResult(false, NotificationStatus.FAILED, message);
        result.setErrorDetails(errorDetails);
        return result;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
}