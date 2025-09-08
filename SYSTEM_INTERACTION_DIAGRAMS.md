# 🔄 Notification System - System Interaction Diagrams

## 📧 **1. Send Immediate Notification Flow**

```mermaid
sequenceDiagram
    participant Client as 🌐 Client/Postman
    participant Controller as 📋 NotificationController
    participant Service as 📧 NotificationService
    participant Producer as 📤 KafkaProducer
    participant Topic as 📨 Kafka Topic
    participant Consumer as 📥 KafkaConsumer
    participant Processor as ⚡ NotificationProcessor
    participant Channel as 📡 EmailChannel
    participant SMTP as 📮 Gmail SMTP
    participant DB as 💾 Database

    Client->>Controller: POST /notifications
    Controller->>Service: sendNotification(request)
    Service->>DB: save(notification)
    DB-->>Service: notification saved
    
    alt Kafka Enabled
        Service->>Producer: publishNotificationEvent(event)
        Producer->>Topic: send to kafka topic
        Topic-->>Producer: ack
        Producer-->>Service: success
        Service-->>Controller: NotificationResponse
        Controller-->>Client: 201 Created
        
        Topic->>Consumer: consume event
        Consumer->>Processor: processNotification(notification)
        Processor->>Channel: send(notification)
        Channel->>SMTP: send email
        SMTP-->>Channel: delivery status
        Channel-->>Processor: result
        Processor->>DB: update status to SENT
    else Kafka Disabled
        Service->>Processor: processNotification(notification)
        Processor->>Channel: send(notification)
        Channel->>SMTP: send email
        SMTP-->>Channel: delivery status
        Channel-->>Processor: result
        Processor->>DB: update status to SENT
        Processor-->>Service: success
        Service-->>Controller: NotificationResponse
        Controller-->>Client: 201 Created
    end
```

## ⏰ **2. Send Scheduled Notification Flow**

```mermaid
sequenceDiagram
    participant Client as 🌐 Client/Postman
    participant Controller as 📋 NotificationController
    participant Service as 📧 NotificationService
    participant Scheduler as ⏰ QuartzScheduler
    participant Job as 🔄 NotificationJob
    participant Processor as ⚡ NotificationProcessor
    participant Channel as 📡 EmailChannel
    participant SMTP as 📮 Gmail SMTP
    participant DB as 💾 Database

    Client->>Controller: POST /notifications (with scheduledAt)
    Controller->>Service: sendNotification(request)
    Service->>DB: save(notification)
    DB-->>Service: notification saved
    Service->>Scheduler: scheduleNotification(notification)
    Scheduler->>Scheduler: create job & trigger
    Scheduler-->>Service: job scheduled
    Service-->>Controller: NotificationResponse
    Controller-->>Client: 201 Created

    Note over Scheduler: Wait until scheduled time...

    Scheduler->>Job: execute job
    Job->>Processor: processScheduledNotificationById(id)
    Processor->>DB: findById(notificationId)
    DB-->>Processor: notification
    Processor->>Channel: send(notification)
    Channel->>SMTP: send email
    SMTP-->>Channel: delivery status
    Channel-->>Processor: result
    Processor->>DB: update status to SENT
    Processor-->>Job: success
    Job->>DB: mark job as completed
```

## 📦 **3. Send Batch Notifications Flow**

```mermaid
sequenceDiagram
    participant Client as 🌐 Client/Postman
    participant Controller as 📋 NotificationController
    participant BatchService as 📦 BatchNotificationService
    participant Service as 📧 NotificationService
    participant Producer as 📤 KafkaProducer
    participant Consumer as 📥 KafkaConsumer
    participant Processor as ⚡ NotificationProcessor
    participant DB as 💾 Database

    Client->>Controller: POST /notifications/batch
    Controller->>BatchService: processBatchNotification(request)
    BatchService->>DB: fetchUsers(userIds)
    DB-->>BatchService: user list
    BatchService->>BatchService: createNotifications(users)
    BatchService->>DB: saveAll(notifications)
    
    par Parallel Processing
        BatchService->>Service: processNotification(notification1)
        Service->>Producer: publishEvent(event1)
        Producer->>Consumer: event1
        Consumer->>Processor: process(notification1)
    and
        BatchService->>Service: processNotification(notification2)
        Service->>Producer: publishEvent(event2)
        Producer->>Consumer: event2
        Consumer->>Processor: process(notification2)
    and
        BatchService->>Service: processNotification(notification3)
        Service->>Producer: publishEvent(event3)
        Producer->>Consumer: event3
        Consumer->>Processor: process(notification3)
    end

    BatchService->>BatchService: collectResults()
    BatchService->>BatchService: calculateStatistics()
    BatchService-->>Controller: BatchNotificationResponse
    Controller-->>Client: 200/207/400 (based on results)
```

## 👥 **4. User Management Flow**

```mermaid
sequenceDiagram
    participant Client as 🌐 Client/Postman
    participant Controller as 📋 UserController
    participant Repository as 💾 UserRepository
    participant DB as 🐘 PostgreSQL

    rect rgb(200, 255, 200)
        Note over Client, DB: Create User Flow
        Client->>Controller: POST /users
        Controller->>Repository: save(user)
        Repository->>DB: INSERT INTO users
        DB-->>Repository: user created
        Repository-->>Controller: saved user
        Controller-->>Client: 201 Created
    end

    rect rgb(200, 220, 255)
        Note over Client, DB: Get All Users Flow
        Client->>Controller: GET /users
        Controller->>Repository: findAll()
        Repository->>DB: SELECT * FROM users
        DB-->>Repository: user list
        Repository-->>Controller: users
        Controller-->>Client: 200 OK
    end

    rect rgb(255, 220, 200)
        Note over Client, DB: Get User By ID Flow
        Client->>Controller: GET /users/{id}
        Controller->>Repository: findById(id)
        Repository->>DB: SELECT * FROM users WHERE id = ?
        DB-->>Repository: user or empty
        alt User Found
            Repository-->>Controller: user
            Controller-->>Client: 200 OK
        else User Not Found
            Repository-->>Controller: empty
            Controller-->>Client: 404 Not Found
        end
    end
```

## ⚙️ **5. Admin Operations Flow**

```mermaid
sequenceDiagram
    participant Client as 🌐 Admin Client
    participant Controller as 📋 AdminController
    participant ChannelFactory as 🏭 ChannelFactory
    participant Scheduler as ⏰ QuartzScheduler
    participant DB as 💾 Database

    rect rgb(240, 240, 255)
        Note over Client, ChannelFactory: Get Available Channels
        Client->>Controller: GET /admin/channels
        Controller->>ChannelFactory: getAllChannels()
        ChannelFactory-->>Controller: channel list
        Controller->>Controller: mapChannelInfo()
        Controller-->>Client: 200 OK (channels info)
    end

    rect rgb(255, 240, 240)
        Note over Client, Controller: Get System Status
        Client->>Controller: GET /admin/status
        Controller->>Controller: buildSystemStatus()
        Controller-->>Client: 200 OK (system status)
    end

    rect rgb(240, 255, 240)
        Note over Client, Scheduler: Get Scheduler Info
        Client->>Controller: GET /admin/scheduler/info
        Controller->>Scheduler: printSchedulerInfo()
        Scheduler-->>Controller: scheduler details
        Controller-->>Client: 200 OK (scheduler info)
    end

    rect rgb(255, 255, 240)
        Note over Client, DB: Cancel Scheduled Job
        Client->>Controller: POST /admin/scheduler/cancel/{id}
        Controller->>Scheduler: cancelScheduledNotification(id)
        Scheduler->>DB: findActiveJob(notificationId)
        DB-->>Scheduler: scheduled job
        alt Job Found
            Scheduler->>Scheduler: deleteJob(jobKey)
            Scheduler->>DB: markJobCompleted()
            Scheduler-->>Controller: true (cancelled)
            Controller-->>Client: 200 OK (cancelled: true)
        else Job Not Found
            Scheduler-->>Controller: false (not found)
            Controller-->>Client: 200 OK (cancelled: false)
        end
    end
```

## 🔍 **6. Query Notification Status Flow**

```mermaid
sequenceDiagram
    participant Client as 🌐 Client/Postman
    participant Controller as 📋 NotificationController
    participant Service as 📧 NotificationService
    participant Repository as 💾 NotificationRepository
    participant DB as 🐘 PostgreSQL

    rect rgb(220, 255, 220)
        Note over Client, DB: Get Single Notification
        Client->>Controller: GET /notifications/{id}
        Controller->>Service: getNotification(id)
        Service->>Repository: findById(id)
        Repository->>DB: SELECT * FROM notifications WHERE id = ?
        DB-->>Repository: notification or empty
        alt Found
            Repository-->>Service: notification
            Service->>Service: mapToResponse()
            Service-->>Controller: NotificationResponse
            Controller-->>Client: 200 OK
        else Not Found
            Repository-->>Service: empty
            Service-->>Controller: empty
            Controller-->>Client: 404 Not Found
        end
    end

    rect rgb(220, 220, 255)
        Note over Client, DB: Get User Notifications (Paginated)
        Client->>Controller: GET /notifications/user/{userId}?page=0&size=10
        Controller->>Service: getUserNotifications(userId, page, size)
        Service->>Repository: findByUser(user, pageable)
        Repository->>DB: SELECT * FROM notifications WHERE user_id = ? LIMIT ? OFFSET ?
        DB-->>Repository: page of notifications
        Repository-->>Service: Page<Notification>
        Service->>Service: mapToResponse()
        Service-->>Controller: Page<NotificationResponse>
        Controller-->>Client: 200 OK (paginated results)
    end
```

## 🏗️ **7. System Architecture Overview**

```mermaid
graph TB
    subgraph "🌐 Client Layer"
        CLIENT[REST API Clients]
        POSTMAN[Postman/Curl]
        WEB[Web Applications]
    end

    subgraph "🎮 Controller Layer"
        NC[📋 NotificationController]
        UC[👥 UserController]
        AC[⚙️ AdminController]
    end

    subgraph "🏢 Service Layer"
        NS[📧 NotificationService]
        BNS[📦 BatchNotificationService]
        NP[⚡ NotificationProcessor]
        NSS[⏰ NotificationSchedulerService]
    end

    subgraph "📡 Channel Layer"
        NCF[🏭 ChannelFactory]
        EMAIL[📧 EmailChannel]
        SMS[📱 SmsChannel]
        PUSH[🔔 PushChannel]
    end

    subgraph "⚡ Async Processing"
        KAFKA_PRODUCER[📤 Kafka Producer]
        KAFKA_TOPICS[📨 Kafka Topics]
        KAFKA_CONSUMER[📥 Kafka Consumer]
        QUARTZ[⏰ Quartz Scheduler]
        JOBS[🔄 Notification Jobs]
    end

    subgraph "💾 Data Layer"
        DB[(🐘 PostgreSQL)]
        TABLES[📊 Tables: users, notifications, scheduled_jobs]
    end

    subgraph "🌍 External Services"
        SMTP[📮 Gmail SMTP]
        TWILIO[📱 Twilio SMS]
        PUSH_SERVICE[🔔 Push Service]
    end

    CLIENT --> NC
    POSTMAN --> UC
    WEB --> AC

    NC --> NS
    NC --> BNS
    UC --> DB
    AC --> NCF
    AC --> NSS

    NS --> NP
    NS --> KAFKA_PRODUCER
    NS --> NSS
    BNS --> NS
    NP --> NCF

    KAFKA_PRODUCER --> KAFKA_TOPICS
    KAFKA_TOPICS --> KAFKA_CONSUMER
    KAFKA_CONSUMER --> NP
    NSS --> QUARTZ
    QUARTZ --> JOBS
    JOBS --> NP

    NCF --> EMAIL
    NCF --> SMS
    NCF --> PUSH

    EMAIL --> SMTP
    SMS --> TWILIO
    PUSH --> PUSH_SERVICE

    NS --> DB
    BNS --> DB
    NP --> DB
    NSS --> DB

    style CLIENT fill:#e1f5fe
    style NC fill:#fff3e0
    style NS fill:#f3e5f5
    style KAFKA_PRODUCER fill:#e8f5e8
    style DB fill:#fce4ec
    style SMTP fill:#e8f5e8
```

## 🔄 **8. Error Handling & Retry Flow**

```mermaid
sequenceDiagram
    participant Consumer as 📥 Kafka Consumer
    participant Processor as ⚡ NotificationProcessor
    participant Channel as 📡 EmailChannel
    participant SMTP as 📮 Gmail SMTP
    participant Producer as 📤 Kafka Producer
    participant RetryTopic as 🔄 Retry Topic
    participant DLQ as 💀 Dead Letter Queue

    Consumer->>Processor: processNotification()
    Processor->>Channel: send(notification)
    Channel->>SMTP: attempt email delivery
    SMTP-->>Channel: ❌ FAILED
    Channel-->>Processor: ❌ delivery failed
    
    alt Retry Count < 3
        Processor->>Producer: publishRetryEvent(event)
        Producer->>RetryTopic: send with retry count++
        Note over RetryTopic: Wait (exponential backoff)
        RetryTopic->>Consumer: retry event
        Consumer->>Processor: processNotification() [RETRY]
        Processor->>Channel: send(notification)
        Channel->>SMTP: attempt email delivery again
        
        alt Retry Successful
            SMTP-->>Channel: ✅ SUCCESS
            Channel-->>Processor: ✅ delivered
            Processor->>Processor: update status to SENT
        else Retry Failed Again
            SMTP-->>Channel: ❌ FAILED AGAIN
            Channel-->>Processor: ❌ still failing
            Note over Processor: Continue retry cycle
        end
    else Max Retries Exceeded
        Processor->>Producer: publishToDlq(event, "Max retries exceeded")
        Producer->>DLQ: send to dead letter queue
        DLQ->>DLQ: store for manual investigation
        Note over DLQ: Admin can investigate and reprocess
    end
```

## 📊 **9. Data Flow Diagram**

```mermaid
flowchart TD
    subgraph "📥 Input Data"
        API_REQUEST[🌐 API Request JSON]
        USER_DATA[👤 User Information]
        NOTIFICATION_CONTENT[📝 Message Content]
        SCHEDULE_TIME[⏰ Schedule Time]
        BATCH_SETTINGS[⚙️ Batch Settings]
    end

    subgraph "🔄 Processing"
        VALIDATION[✅ Data Validation]
        ENTITY_CREATION[📝 Entity Creation]
        ROUTING_DECISION[🎯 Routing Decision]
        CHANNEL_SELECTION[📡 Channel Selection]
        DELIVERY_ATTEMPT[📤 Delivery Attempt]
    end

    subgraph "💾 Data Storage"
        USER_TABLE[(👥 users)]
        NOTIFICATION_TABLE[(📧 notifications)]
        SCHEDULED_JOB_TABLE[(⏰ scheduled_jobs)]
        AUDIT_LOG[(📋 audit_logs)]
    end

    subgraph "📤 Output Data"
        EMAIL_MESSAGE[📧 Email Message]
        SMS_MESSAGE[📱 SMS Message]
        PUSH_NOTIFICATION[🔔 Push Notification]
        API_RESPONSE[🔄 API Response]
        STATUS_UPDATES[📊 Status Updates]
    end

    API_REQUEST --> VALIDATION
    USER_DATA --> VALIDATION
    NOTIFICATION_CONTENT --> VALIDATION
    SCHEDULE_TIME --> VALIDATION
    BATCH_SETTINGS --> VALIDATION

    VALIDATION --> ENTITY_CREATION
    ENTITY_CREATION --> USER_TABLE
    ENTITY_CREATION --> NOTIFICATION_TABLE
    ENTITY_CREATION --> SCHEDULED_JOB_TABLE

    ENTITY_CREATION --> ROUTING_DECISION
    ROUTING_DECISION --> CHANNEL_SELECTION
    CHANNEL_SELECTION --> DELIVERY_ATTEMPT

    DELIVERY_ATTEMPT --> EMAIL_MESSAGE
    DELIVERY_ATTEMPT --> SMS_MESSAGE
    DELIVERY_ATTEMPT --> PUSH_NOTIFICATION
    DELIVERY_ATTEMPT --> API_RESPONSE
    DELIVERY_ATTEMPT --> STATUS_UPDATES
    DELIVERY_ATTEMPT --> AUDIT_LOG

    style API_REQUEST fill:#e1f5fe
    style EMAIL_MESSAGE fill:#e8f5e8
    style USER_TABLE fill:#fce4ec
    style ROUTING_DECISION fill:#fff3e0
```

---

## 📋 **Component Interaction Summary**

| 🎯 **Flow Type** | 🔄 **Key Components** | 📊 **Data Flow** |
|------------------|----------------------|------------------|
| **📧 Immediate** | Controller → Service → Kafka → Consumer → Channel | Async via Kafka topics |
| **⏰ Scheduled** | Controller → Service → Quartz → Job → Channel | Delayed execution |
| **📦 Batch** | Controller → BatchService → Multiple async flows | Parallel/Sequential processing |
| **👥 User CRUD** | Controller → Repository → Database | Direct database operations |
| **⚙️ Admin** | Controller → Various Services → System info | System introspection |

This provides clear visual interaction flows that you can easily view in any markdown viewer that supports Mermaid diagrams! 🎉