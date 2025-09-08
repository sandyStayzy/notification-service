# 🏗️ Notification System - Complete API Flow Diagrams

## 📊 System Architecture Overview

```mermaid
graph TB
    subgraph "External Clients"
        API[REST API Clients]
        POSTMAN[Postman/Curl]
    end

    subgraph "Controllers Layer"
        NC[NotificationController]
        UC[UserController] 
        AC[AdminController]
    end

    subgraph "Service Layer"
        NS[NotificationService]
        BNS[BatchNotificationService]
        NP[NotificationProcessor]
        NSS[NotificationSchedulerService]
    end

    subgraph "Channel Layer"
        NCF[NotificationChannelFactory]
        EMAIL[EmailChannel]
        SMS[SmsChannel] 
        PUSH[PushChannel]
    end

    subgraph "Infrastructure"
        DB[(PostgreSQL)]
        KAFKA[Kafka Topics]
        QUARTZ[Quartz Scheduler]
        SMTP[Gmail SMTP]
    end

    API --> NC
    API --> UC
    API --> AC
    
    NC --> NS
    NC --> BNS
    NS --> NP
    BNS --> NP
    NS --> NSS
    NP --> NCF
    NCF --> EMAIL
    NCF --> SMS
    NCF --> PUSH
    
    NS --> KAFKA
    KAFKA --> NP
    NSS --> QUARTZ
    QUARTZ --> NP
    
    EMAIL --> SMTP
    NS --> DB
    BNS --> DB
    UC --> DB
```

---

## 🔥 All API Endpoints Summary

| Method | Endpoint | Description | Flow Type |
|--------|----------|-------------|-----------|
| `POST` | `/api/v1/notifications` | Send notification | Immediate/Scheduled |
| `GET` | `/api/v1/notifications/{id}` | Get notification by ID | Simple Query |
| `GET` | `/api/v1/notifications/user/{userId}` | Get user notifications | Paginated Query |
| `POST` | `/api/v1/notifications/batch` | Send batch notifications | Complex Batch |
| `POST` | `/api/v1/users` | Create user | Simple CRUD |
| `GET` | `/api/v1/users` | Get all users | Simple Query |
| `GET` | `/api/v1/users/{id}` | Get user by ID | Simple Query |
| `GET` | `/api/v1/admin/channels` | Get available channels | Admin Info |
| `GET` | `/api/v1/admin/status` | Get system status | Admin Info |
| `GET` | `/api/v1/admin/scheduler/info` | Get scheduler info | Admin Info |
| `POST` | `/api/v1/admin/scheduler/cancel/{id}` | Cancel scheduled notification | Admin Action |

---

## 🎯 Key Flow Patterns

### 🚀 **Pattern 1: Immediate Notifications (with Kafka)**
```
API Request → NotificationService → Kafka Producer → Kafka Topic → Kafka Consumer → NotificationProcessor → Channel → SMTP/Console → Response
```

### ⏰ **Pattern 2: Scheduled Notifications (with Quartz)**
```
API Request → NotificationService → Quartz Scheduler → [Wait for Schedule] → NotificationJob → NotificationProcessor → Channel → SMTP/Console
```

### 📦 **Pattern 3: Batch Notifications**
```
API Request → BatchNotificationService → Create Multiple Notifications → [Parallel/Sequential Processing] → Individual Notification Flow → Aggregate Results → Response
```

### 📊 **Pattern 4: Simple CRUD Operations**
```
API Request → Controller → Repository → Database → Response
```

---

## 🔥 **1. POST /api/v1/notifications - Send Notification**

```mermaid
flowchart TD
    START([🌐 API Request]) --> VALIDATE{✅ Valid Request?}
    VALIDATE -->|❌ No| ERROR_400[🚫 Return 400 Bad Request]
    VALIDATE -->|✅ Yes| FIND_USER{👤 User Exists?}
    
    FIND_USER -->|❌ No| ERROR_404[🚫 Return 404 User Not Found]
    FIND_USER -->|✅ Yes| CREATE_NOTIF[📝 Create Notification Entity]
    
    CREATE_NOTIF --> SAVE_DB[💾 Save to Database]
    SAVE_DB --> CHECK_SCHEDULED{⏰ Scheduled?}
    
    CHECK_SCHEDULED -->|✅ Yes| QUARTZ_FLOW[🕐 Schedule with Quartz]
    QUARTZ_FLOW --> QUARTZ_SCHEDULER[⚙️ NotificationSchedulerService]
    QUARTZ_SCHEDULER --> SCHEDULE_JOB[📅 Create Quartz Job]
    SCHEDULE_JOB --> FUTURE_EXEC[⏳ Execute at Scheduled Time]
    FUTURE_EXEC --> QUARTZ_EXEC[🔄 NotificationJob.execute]
    QUARTZ_EXEC --> DIRECT_PROCESS[⚡ NotificationProcessor]
    
    CHECK_SCHEDULED -->|❌ No| KAFKA_CHECK{📨 Kafka Enabled?}
    KAFKA_CHECK -->|✅ Yes| KAFKA_PRODUCER[📤 Publish to Kafka]
    KAFKA_PRODUCER --> PRIORITY_TOPIC{🔥 Priority?}
    PRIORITY_TOPIC -->|HIGH| HIGH_TOPIC[🚀 notification-events-high-priority]
    PRIORITY_TOPIC -->|MEDIUM/LOW| NORMAL_TOPIC[📬 notification-events]
    HIGH_TOPIC --> KAFKA_CONSUMER[📥 Kafka Consumer]
    NORMAL_TOPIC --> KAFKA_CONSUMER
    KAFKA_CONSUMER --> ASYNC_PROCESS[⚡ NotificationProcessor]
    
    KAFKA_CHECK -->|❌ No| SYNC_PROCESS[🔄 Direct Processing]
    
    DIRECT_PROCESS --> CHANNEL_FACTORY[🏭 NotificationChannelFactory]
    ASYNC_PROCESS --> CHANNEL_FACTORY
    SYNC_PROCESS --> CHANNEL_FACTORY
    
    CHANNEL_FACTORY --> SELECT_CHANNEL{📡 Channel Type}
    SELECT_CHANNEL -->|📧 EMAIL| EMAIL_CHANNEL[📧 EmailChannel]
    SELECT_CHANNEL -->|📱 SMS| SMS_CHANNEL[📱 SmsChannel]  
    SELECT_CHANNEL -->|🔔 PUSH| PUSH_CHANNEL[🔔 PushChannel]
    
    EMAIL_CHANNEL --> SMTP_SEND[📮 Gmail SMTP]
    SMS_CHANNEL --> CONSOLE_SMS[🖥️ Console Log]
    PUSH_CHANNEL --> CONSOLE_PUSH[🖥️ Console Log]
    
    SMTP_SEND --> UPDATE_STATUS[✅ Update Status to SENT]
    CONSOLE_SMS --> UPDATE_STATUS
    CONSOLE_PUSH --> UPDATE_STATUS
    
    UPDATE_STATUS --> RESPONSE_201[🎉 Return 201 Created]
    
    QUARTZ_FLOW --> RESPONSE_201
    KAFKA_PRODUCER --> RESPONSE_201
    SYNC_PROCESS --> RESPONSE_201

    style START fill:#e1f5fe
    style RESPONSE_201 fill:#c8e6c9
    style ERROR_400 fill:#ffcdd2
    style ERROR_404 fill:#ffcdd2
    style KAFKA_PRODUCER fill:#fff3e0
    style QUARTZ_SCHEDULER fill:#f3e5f5
    style SMTP_SEND fill:#e8f5e8
```

**Key Decision Points:**
- ⏰ **Scheduled?** → Use Quartz Scheduler (bypass Kafka)
- 📨 **Kafka Enabled?** → Async via Kafka OR Direct processing
- 🔥 **High Priority?** → Dedicated high-priority Kafka topic
- 📧 **Channel Type?** → EMAIL (real SMTP) vs SMS/PUSH (console)

---

## 📦 **2. POST /api/v1/notifications/batch - Batch Notifications**

```mermaid
flowchart TD
    START([🌐 Batch API Request]) --> VALIDATE{✅ Valid Request?}
    VALIDATE -->|❌ No| ERROR_400[🚫 Return 400 Bad Request]
    VALIDATE -->|✅ Yes| GENERATE_ID[🆔 Generate Batch ID]
    
    GENERATE_ID --> FETCH_USERS[👥 Fetch Valid Users]
    FETCH_USERS --> CHECK_USERS{👤 Users Found?}
    CHECK_USERS -->|❌ No| BATCH_FAILED[❌ Status: FAILED]
    
    CHECK_USERS -->|✅ Yes| CREATE_NOTIFS[📝 Create Notifications for Each User]
    CREATE_NOTIFS --> SAVE_ALL[💾 Save All to Database]
    
    SAVE_ALL --> BATCH_SCHEDULED{⏰ Batch Scheduled?}
    BATCH_SCHEDULED -->|✅ Yes| SCHEDULE_ALL[📅 Schedule All with Quartz]
    SCHEDULE_ALL --> BATCH_RESPONSE[🎉 Return Batch Response]
    
    BATCH_SCHEDULED -->|❌ No| BATCH_SETTINGS{⚡ Parallel Processing?}
    
    BATCH_SETTINGS -->|✅ Yes| PARALLEL_PROCESS[🚀 Process in Parallel Batches]
    PARALLEL_PROCESS --> SPLIT_BATCHES[📊 Split into Batch Size Chunks]
    SPLIT_BATCHES --> CONCURRENT_EXEC[🔄 CompletableFuture Execution]
    
    BATCH_SETTINGS -->|❌ No| SEQUENTIAL_PROCESS[🔢 Process Sequentially]
    SEQUENTIAL_PROCESS --> DELAY_BETWEEN[⏱️ Add Delay Between Batches]
    
    CONCURRENT_EXEC --> PROCESS_NOTIF[⚡ Process Each Notification]
    DELAY_BETWEEN --> PROCESS_NOTIF
    
    PROCESS_NOTIF --> KAFKA_ENABLED{📨 Kafka Enabled?}
    KAFKA_ENABLED -->|✅ Yes| TO_KAFKA[📤 Send to Kafka]
    KAFKA_ENABLED -->|❌ No| DIRECT_PROCESS[🔄 Direct Processing]
    
    TO_KAFKA --> KAFKA_CONSUMER[📥 Kafka Consumer Processing]
    KAFKA_CONSUMER --> SEND_EMAIL[📧 Channel Processing]
    DIRECT_PROCESS --> SEND_EMAIL
    
    SEND_EMAIL --> COLLECT_RESULTS[📊 Collect All Results]
    COLLECT_RESULTS --> CALCULATE_STATS[📈 Calculate Success/Failure Stats]
    
    CALCULATE_STATS --> FINAL_STATUS{📊 All Successful?}
    FINAL_STATUS -->|✅ Yes| STATUS_COMPLETED[🎉 Status: COMPLETED - 200 OK]
    FINAL_STATUS -->|⚠️ Partial| STATUS_PARTIAL[⚠️ Status: PARTIALLY_FAILED - 207 Multi-Status]
    FINAL_STATUS -->|❌ None| STATUS_FAILED[❌ Status: FAILED - 400 Bad Request]
    
    BATCH_FAILED --> STATUS_FAILED
    SCHEDULE_ALL --> STATUS_COMPLETED
    
    STATUS_COMPLETED --> BATCH_RESPONSE
    STATUS_PARTIAL --> BATCH_RESPONSE
    STATUS_FAILED --> BATCH_RESPONSE

    style START fill:#e1f5fe
    style BATCH_RESPONSE fill:#c8e6c9
    style ERROR_400 fill:#ffcdd2
    style PARALLEL_PROCESS fill:#fff3e0
    style SEQUENTIAL_PROCESS fill:#f3e5f5
```

**Batch Processing Features:**
- 🚀 **Parallel Processing**: Uses `CompletableFuture` for concurrent execution
- 📊 **Batch Size Control**: Configurable chunk sizes
- ⏱️ **Delay Control**: Optional delays between batches
- 📈 **Statistics**: Tracks success/failure rates
- 🎯 **Continue on Error**: Optional error tolerance

---

## 📋 **Simple Query Endpoints**

### **3. GET /api/v1/notifications/{id}**
```mermaid
flowchart LR
    START([🌐 GET Request]) --> EXTRACT[🆔 Extract ID] --> FIND{🔍 Exists?} 
    FIND -->|❌| ERROR_404[🚫 404 Not Found]
    FIND -->|✅| RESPONSE_200[🎉 200 OK]
```

### **4. GET /api/v1/notifications/user/{userId}**
```mermaid
flowchart LR
    START([🌐 GET Request]) --> EXTRACT[🆔 Extract User ID + Pagination] --> FIND{👤 User Exists?}
    FIND -->|❌| ERROR_404[🚫 404 User Not Found]
    FIND -->|✅| QUERY[📄 Paginated Query] --> RESPONSE_200[🎉 200 OK with Page]
```

### **5-7. User Management Endpoints**
```mermaid
flowchart LR
    subgraph "User CRUD Operations"
        POST_USER[📝 POST /users<br/>Create User] --> DB[(💾 Database)]
        GET_USERS[📋 GET /users<br/>Get All Users] --> DB
        GET_USER[👤 GET /users/{id}<br/>Get User by ID] --> DB
    end
    DB --> SUCCESS[✅ Success Response]
```

---

## ⚙️ **Admin Endpoints**

### **8. GET /api/v1/admin/channels**
```mermaid
flowchart LR
    START([🌐 GET Request]) --> FACTORY[🏭 Get All Channels] --> MAP[🗺️ Map Channel Info] --> RESPONSE[📋 Channel List]
```

### **9. GET /api/v1/admin/status**
```mermaid
flowchart LR
    START([🌐 GET Request]) --> BUILD[🏗️ Build Status] --> RESPONSE[📊 System Status]
```

### **10. GET /api/v1/admin/scheduler/info**
```mermaid
flowchart LR
    START([🌐 GET Request]) --> PRINT[🖨️ Print to Logs] --> BUILD[🏗️ Build Response] --> RESPONSE[📊 Scheduler Info]
```

### **11. POST /api/v1/admin/scheduler/cancel/{id}**
```mermaid
flowchart TD
    START([🌐 POST Request]) --> EXTRACT[🆔 Extract ID] --> FIND{🔍 Job Exists?}
    FIND -->|❌| NOT_FOUND[❌ cancelled: false] --> RESPONSE[📋 200 OK with Result]
    FIND -->|✅| DELETE[🗑️ Delete Quartz Job] --> UPDATE[📝 Update DB] --> SUCCESS[✅ cancelled: true] --> RESPONSE
```

---

## 🔄 **Kafka Flow Details**

### **Kafka Topics & Consumers**
```mermaid
graph TD
    subgraph "Kafka Infrastructure"
        PRODUCER[📤 NotificationEventProducer]
        
        subgraph "Topics"
            HIGH_TOPIC[🚀 notification-events-high-priority]
            NORMAL_TOPIC[📬 notification-events]
            RETRY_TOPIC[🔄 notification-events-retry]
            DLQ_TOPIC[💀 notification-events-dlq]
        end
        
        subgraph "Consumers"
            HIGH_CONSUMER[📥 High Priority Consumer]
            NORMAL_CONSUMER[📥 Normal Consumer]
            RETRY_CONSUMER[📥 Retry Consumer]
            DLQ_CONSUMER[📥 DLQ Consumer]
        end
    end
    
    PRODUCER --> HIGH_TOPIC
    PRODUCER --> NORMAL_TOPIC
    PRODUCER --> RETRY_TOPIC
    PRODUCER --> DLQ_TOPIC
    
    HIGH_TOPIC --> HIGH_CONSUMER
    NORMAL_TOPIC --> NORMAL_CONSUMER
    RETRY_TOPIC --> RETRY_CONSUMER
    DLQ_TOPIC --> DLQ_CONSUMER
    
    HIGH_CONSUMER --> PROCESS[⚡ NotificationProcessor]
    NORMAL_CONSUMER --> PROCESS
    RETRY_CONSUMER --> PROCESS
    DLQ_CONSUMER --> LOG[📝 Log for Investigation]
```

**Kafka Features:**
- 🔥 **Priority-based Topics**: High priority gets dedicated topic
- 🔄 **Retry Mechanism**: Automatic retry with exponential backoff
- 💀 **Dead Letter Queue**: Failed messages for investigation
- ⚡ **Async Processing**: Non-blocking notification delivery

---

## 🕐 **Quartz Scheduler Flow**

### **Scheduled Notification Lifecycle**
```mermaid
sequenceDiagram
    participant API as 🌐 API Client
    participant NS as 📧 NotificationService
    participant QS as ⏰ QuartzScheduler
    participant JOB as 🔄 NotificationJob
    participant NP as ⚡ NotificationProcessor
    participant CH as 📧 EmailChannel
    participant SMTP as 📮 Gmail SMTP
    
    API->>NS: POST /notifications (scheduledAt: future)
    NS->>NS: Create & Save Notification
    NS->>QS: Schedule with Quartz
    QS->>QS: Create Job & Trigger
    Note over QS: Job waits until scheduled time
    QS->>JOB: Execute at scheduled time
    JOB->>NP: Process notification by ID
    NP->>CH: Send via appropriate channel
    CH->>SMTP: Send email
    SMTP-->>CH: Success/Failure
    CH-->>NP: Result
    NP->>NP: Update notification status
    JOB->>JOB: Mark job as completed
```

**Quartz Features:**
- ⏰ **Precise Scheduling**: Execute at exact future times
- 🔄 **Job Persistence**: Jobs survive application restarts
- 🚫 **Cancellation**: Cancel scheduled jobs via admin API
- 🔧 **Misfire Handling**: Smart handling of missed executions
- 📊 **Job Tracking**: Full lifecycle monitoring

---

## 📈 **Performance & Scaling Features**

### **Concurrency & Async Processing**
```mermaid
graph TD
    subgraph "Processing Models"
        SYNC[🔄 Synchronous<br/>Direct Processing]
        ASYNC_KAFKA[📨 Asynchronous<br/>Kafka-based]
        SCHEDULED[⏰ Scheduled<br/>Quartz-based]
        BATCH_PARALLEL[🚀 Batch Parallel<br/>CompletableFuture]
        BATCH_SEQUENTIAL[🔢 Batch Sequential<br/>Controlled Rate]
    end
    
    subgraph "Infrastructure"
        DB[(💾 PostgreSQL)]
        KAFKA_CLUSTER[📨 Kafka Cluster]
        QUARTZ_STORE[⏰ Quartz Job Store]
        SMTP_POOL[📮 SMTP Connection Pool]
    end
    
    SYNC --> DB
    ASYNC_KAFKA --> KAFKA_CLUSTER
    ASYNC_KAFKA --> DB
    SCHEDULED --> QUARTZ_STORE
    SCHEDULED --> DB
    BATCH_PARALLEL --> DB
    BATCH_SEQUENTIAL --> DB
    
    KAFKA_CLUSTER --> SMTP_POOL
    QUARTZ_STORE --> SMTP_POOL
```

---

## 🛡️ **Error Handling & Reliability**

### **Error Response Codes**
| Code | Scenario | Description |
|------|----------|-------------|
| `200` | Success | Operation completed successfully |
| `201` | Created | Notification/User created successfully |
| `207` | Multi-Status | Batch partially failed |
| `400` | Bad Request | Invalid request data |
| `404` | Not Found | Resource not found |
| `500` | Server Error | Internal server error |

### **Retry Mechanisms**
```mermaid
graph TD
    FAIL[❌ Processing Failed] --> RETRY_CHECK{🔄 Should Retry?}
    RETRY_CHECK -->|✅ Yes| RETRY_COUNT{📊 Retry Count < 3?}
    RETRY_COUNT -->|✅ Yes| EXPONENTIAL[⏰ Exponential Backoff]
    EXPONENTIAL --> RETRY_TOPIC[🔄 Kafka Retry Topic]
    RETRY_TOPIC --> PROCESS_AGAIN[⚡ Process Again]
    
    RETRY_COUNT -->|❌ No| DLQ[💀 Dead Letter Queue]
    RETRY_CHECK -->|❌ No| LOG[📝 Log Error]
    
    PROCESS_AGAIN --> SUCCESS[✅ Success]
    PROCESS_AGAIN --> FAIL
    
    style FAIL fill:#ffcdd2
    style SUCCESS fill:#c8e6c9
    style DLQ fill:#fff3e0
```

---

## 🎯 **Usage Examples**

### **1. Send Immediate High-Priority Email**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "🚨 Critical Alert",
    "content": "System maintenance in 5 minutes",
    "channelType": "EMAIL",
    "priority": "HIGH"
  }'
```
**Flow**: API → NotificationService → Kafka High-Priority Topic → Consumer → EmailChannel → Gmail SMTP

### **2. Schedule Email for Future**
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "📅 Scheduled Reminder",
    "content": "Meeting tomorrow at 2 PM",
    "channelType": "EMAIL",
    "priority": "MEDIUM",
    "scheduledAt": "2025-09-08T14:00:00"
  }'
```
**Flow**: API → NotificationService → Quartz Scheduler → [Wait] → NotificationJob → EmailChannel → Gmail SMTP

### **3. Send Batch Notifications**
```bash
curl -X POST http://localhost:8080/api/v1/notifications/batch \
  -H "Content-Type: application/json" \
  -d '{
    "userIds": [1, 2, 3, 4, 5],
    "title": "📢 System Update",
    "content": "New features available",
    "channelType": "EMAIL",
    "priority": "MEDIUM",
    "batchSettings": {
      "batchSize": 2,
      "delayBetweenBatches": 1000,
      "parallelProcessing": true,
      "continueOnError": true
    }
  }'
```
**Flow**: API → BatchNotificationService → [Create 5 Notifications] → [Process in Batches of 2] → Kafka/Direct → EmailChannel → Gmail SMTP

---

## 📊 **System Configuration**

### **Key Configuration Properties**
```yaml
notification:
  kafka:
    enabled: true  # Enable/disable Kafka processing
    bootstrap-servers: localhost:9092
  channels:
    email:
      smtp:
        enabled: true  # Use real SMTP
      console:
        enabled: true  # Also log to console
    sms:
      twilio:
        enabled: false  # Use console mock
      console:
        enabled: true
```

### **Infrastructure Requirements**
- ☕ **Java 17+**
- 🐘 **PostgreSQL 12+** for data persistence
- 📨 **Apache Kafka** for async processing (optional)
- 📮 **SMTP Server** (Gmail) for email delivery
- 🚀 **Spring Boot 3.1.5** framework

---

This documentation provides a complete visual guide to your notification system's architecture and API flows! 🎉