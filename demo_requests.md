# Notification System - Demo API Requests

This document provides example API requests to demonstrate all the features of the notification system.

## Base URL
```
http://localhost:8080
```

## Demo Workflow

### 1. Check System Status
```bash
curl -X GET http://localhost:8080/api/v1/admin/status
```

### 2. Check Available Channels
```bash
curl -X GET http://localhost:8080/api/v1/admin/channels
```

### 3. Create Test Users

**Create User 1 (Full Contact Info)**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com", 
    "password": "password123",
    "phoneNumber": "+1-555-123-4567"
  }'
```

**Create User 2 (Email Only)**
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_smith",
    "email": "jane.smith@example.com",
    "password": "password456"
  }'
```

### 4. Send Different Types of Notifications

**Email Notification (High Priority)**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "🚀 Welcome to Our Platform!",
    "content": "Thank you for joining! We are excited to have you on board. Get ready for an amazing journey with our notification system.",
    "channelType": "EMAIL",
    "priority": "HIGH",
    "metadata": {
      "campaign": "welcome_series",
      "version": "v2.0",
      "source": "registration"
    }
  }'
```

**SMS Notification (Medium Priority)**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "📱 Account Verification",
    "content": "Your account verification code is: 123456. Please use this code within 10 minutes.",
    "channelType": "SMS",
    "priority": "MEDIUM",
    "metadata": {
      "type": "verification",
      "expires_in": "10_minutes"
    }
  }'
```

**Push Notification (High Priority)**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "title": "🔔 Breaking News!",
    "content": "Important system maintenance scheduled for tonight at 2 AM EST. Please save your work.",
    "channelType": "PUSH",
    "priority": "HIGH",
    "metadata": {
      "category": "system_alert",
      "action_required": true,
      "maintenance_window": "2024-12-20T02:00:00Z"
    }
  }'
```

**Scheduled Email Notification**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "📅 Scheduled Reminder",
    "content": "This is your friendly reminder about the upcoming webinar tomorrow at 3 PM EST. Do not forget to join!",
    "channelType": "EMAIL", 
    "priority": "LOW",
    "scheduledAt": "2024-12-25T15:00:00",
    "metadata": {
      "event_type": "webinar",
      "reminder_type": "24h_before"
    }
  }'
```

**SMS to User Without Phone (Should Fail)**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "title": "SMS Test",
    "content": "This SMS should fail because user has no phone number",
    "channelType": "SMS",
    "priority": "MEDIUM"
  }'
```

### 5. Send Batch Notifications

**Basic Batch Notification (Sequential Processing)**
```bash
curl -X POST http://localhost:8080/api/v1/notifications/batch \
  -H "Content-Type: application/json" \
  -d '{
    "userIds": [1, 2],
    "title": "📢 System Maintenance Notice",
    "content": "Our system will undergo scheduled maintenance on Sunday from 2:00 AM to 4:00 AM EST. During this time, some services may be temporarily unavailable. We apologize for any inconvenience.",
    "channelType": "EMAIL",
    "priority": "HIGH",
    "batchSettings": {
      "batchSize": 5,
      "delayBetweenBatches": 1000,
      "parallelProcessing": false,
      "continueOnError": true
    }
  }'
```

**Advanced Batch with Parallel Processing**
```bash
curl -X POST http://localhost:8080/api/v1/notifications/batch \
  -H "Content-Type: application/json" \
  -d '{
    "userIds": [1, 2],
    "title": "🎉 New Feature Release",
    "content": "We are excited to announce our new batch notification feature! You can now send notifications to multiple users simultaneously with advanced processing options.",
    "channelType": "EMAIL",
    "priority": "MEDIUM",
    "metadata": {
      "feature": "batch_notifications",
      "version": "1.0.0",
      "release_date": "2024-12-20"
    },
    "batchSettings": {
      "batchSize": 10,
      "delayBetweenBatches": 500,
      "parallelProcessing": true,
      "continueOnError": true
    }
  }'
```

**Scheduled Batch Notification**
```bash
curl -X POST http://localhost:8080/api/v1/notifications/batch \
  -H "Content-Type: application/json" \
  -d '{
    "userIds": [1, 2],
    "title": "⏰ Scheduled Batch Reminder",
    "content": "This is a scheduled batch notification that will be sent to all users at the specified time.",
    "channelType": "EMAIL",
    "priority": "LOW",
    "scheduledAt": "2024-12-25T10:00:00",
    "batchSettings": {
      "batchSize": 20,
      "delayBetweenBatches": 2000,
      "parallelProcessing": false,
      "continueOnError": true
    }
  }'
```

### 6. Check Notification Status

**Get Specific Notification**
```bash
curl -X GET http://localhost:8080/api/v1/notifications/1
```

**Get All Notifications for User 1**
```bash
curl -X GET "http://localhost:8080/api/v1/notifications/user/1?page=0&size=10"
```

**Get All Notifications for User 2**
```bash
curl -X GET "http://localhost:8080/api/v1/notifications/user/2?page=0&size=10"
```

### 6. Get All Users
```bash
curl -X GET http://localhost:8080/api/v1/users
```

## Expected Console Output

When you send notifications, you should see detailed output in the console showing:

### Email Channel Output:
```
============================================================
📧 EMAIL NOTIFICATION SENT
============================================================
To: john.doe@example.com
From: noreply@notificationservice.com
Subject: 🚀 Welcome to Our Platform!
Priority: HIGH
Sent At: 2024-12-20T10:30:00

Content:
----------------------------------------
Thank you for joining! We are excited to have you on board. Get ready for an amazing journey with our notification system.
----------------------------------------

Metadata: {campaign=welcome_series, version=v2.0, source=registration}
============================================================
```

### SMS Channel Output:
```
============================================================
📱 SMS NOTIFICATION SENT
============================================================
To: +1-555-123-4567
From: +1-555-NOTIFY
Priority: MEDIUM
Sent At: 2024-12-20T10:31:00

Message:
----------------------------------------
📱 Account Verification
Your account verification code is: 123456. Please use this code within 10 minutes.
----------------------------------------

Metadata: {type=verification, expires_in=10_minutes}
Character Count: 89
============================================================
```

### Batch Notification Output:
```
11:15:21.841 [http-nio-8080-exec-1] INFO  BatchNotificationService - 📦 Starting batch notification processing: batch_1757223921841 for 2 users
11:15:21.918 [http-nio-8080-exec-1] DEBUG NotificationProcessor - 🔄 Processing notification: System Maintenance Notice (ID: 18)
============================================================
📧 SMTP EMAIL SENT
============================================================
To: sandeep.yadav1205@gmail.com
From: noreply@notificationservice.com
Subject: System Maintenance Notice
Priority: HIGH
Sent At: 2025-09-07T11:15:25.829

Content:
----------------------------------------
Our system will undergo scheduled maintenance on Sunday from 2:00 AM to 4:00 AM EST. During this time, some services may be temporarily unavailable.
----------------------------------------
Email Format: Rich HTML with styling
============================================================

11:15:25.829 [http-nio-8080-exec-1] INFO  SmtpEmailChannel - SMTP email sent successfully to sandeep.yadav1205@gmail.com
11:15:25.829 [http-nio-8080-exec-1] INFO  NotificationProcessor - ✅ Notification sent successfully via SMTP Email Channel: System Maintenance Notice

Response JSON:
{
  "batchId": "batch_1757223921841",
  "totalUsers": 2,
  "successCount": 2,
  "failureCount": 0,
  "status": "COMPLETED",
  "results": [
    {
      "userId": 9,
      "notificationId": 18,
      "success": true,
      "message": "Sent successfully",
      "processedAt": "2025-09-07T11:15:25.830563"
    }
  ],
  "processingTimeMs": 7833,
  "statistics": {
    "totalBatches": 1,
    "processedBatches": 1,
    "successRate": 100.0,
    "errorBreakdown": {}
  }
}
```

### Push Channel Output:
```
============================================================
📲 PUSH NOTIFICATION SENT
============================================================
User: jane_smith
Device Token: device_a1b2c3d4
Title: 🔔 Breaking News!
Priority: HIGH
Sent At: 2024-12-20T10:32:00

Payload:
----------------------------------------
{
  "title": "🔔 Breaking News!",
  "body": "Important system maintenance scheduled for tonight at 2 AM EST. Please save your work.",
  "priority": "high",
  "data": {category=system_alert, action_required=true, maintenance_window=2024-12-20T02:00:00Z}
}
----------------------------------------
============================================================
```

## Testing Priority Handling

The system demonstrates priority-based processing. You can send multiple notifications with different priorities to observe the behavior:

**Send Multiple Notifications with Different Priorities**
```bash
# High Priority
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "🔥 URGENT", "content": "High priority message", "channelType": "EMAIL", "priority": "HIGH"}'

# Low Priority  
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "📝 Info", "content": "Low priority message", "channelType": "EMAIL", "priority": "LOW"}'

# Medium Priority
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "title": "⚠️ Notice", "content": "Medium priority message", "channelType": "EMAIL", "priority": "MEDIUM"}'
```

## API Documentation

Once the application is running, visit the interactive API documentation:
```
http://localhost:8080/swagger-ui/index.html
```

This provides a complete interface to test all endpoints with proper request/response examples.