# 🔄 Notification System - Visual Flow Diagrams

## 📧 **1. Send Immediate Notification Flow**

```
🌐 Client/Postman
        |
        | POST /notifications
        ↓
📋 NotificationController
        |
        | sendNotification(request)
        ↓
📧 NotificationService
        |
        | save(notification)
        ↓
💾 Database ────────────── notification saved ──────────────┐
        |                                                   |
        ↓                                                   |
❓ Kafka Enabled?                                           |
        |                                                   |
   ┌────┴────┐                                             |
   |         |                                             |
✅ YES      ❌ NO                                          |
   |         |                                             |
   ↓         ↓                                             |
📤 Kafka    🔄 Direct                                      |
Producer    Processing                                      |
   |         |                                             |
   ↓         ↓                                             |
📨 Kafka    ⚡ Notification                                |
Topic       Processor                                      |
   |         |                                             |
   ↓         ↓                                             |
📥 Kafka    📡 EmailChannel                               |
Consumer    |                                             |
   |         ↓                                             |
   ↓      📮 Gmail SMTP                                    |
⚡ Notification                                            |
Processor   |                                             |
   |         ↓                                             |
   ↓      ✅ Email Sent                                    |
📡 EmailChannel                                           |
   |                                                       |
   ↓                                                       |
📮 Gmail SMTP                                             |
   |                                                       |
   ↓                                                       |
✅ Email Sent ────────────────────────────────────────────┘
   |
   ↓
🎉 201 Created Response
```

---

## ⏰ **2. Send Scheduled Notification Flow**

```
🌐 Client/Postman
        |
        | POST /notifications (scheduledAt: "2025-09-08T10:00:00")
        ↓
📋 NotificationController
        |
        | sendNotification(request)
        ↓
📧 NotificationService
        |
        | save(notification)
        ↓
💾 Database ────────────── notification saved
        |
        | scheduleNotification(notification)
        ↓
⏰ QuartzScheduler
        |
        | create job & trigger
        ↓
📅 Job Scheduled ──────── job scheduled ────────┐
        |                                        |
        ↓                                        |
🎉 201 Created Response                         |
                                                |
        ⏳ Wait until scheduled time...          |
                                                |
⏰ QuartzScheduler ←────────────────────────────┘
        |
        | execute job
        ↓
🔄 NotificationJob
        |
        | processScheduledNotificationById(id)
        ↓
⚡ NotificationProcessor
        |
        | findById(notificationId)
        ↓
💾 Database ────────────── notification found
        |
        ↓
📡 EmailChannel
        |
        | send(notification)
        ↓
📮 Gmail SMTP
        |
        ↓
✅ Email Delivered
        |
        ↓
💾 Database ────────────── status updated to SENT
        |
        ↓
📋 Job Marked as Completed
```

---

## 📦 **3. Send Batch Notifications Flow**

```
🌐 Client/Postman
        |
        | POST /notifications/batch
        | { "userIds": [1, 2, 3], "title": "Batch Message" }
        ↓
📋 NotificationController
        |
        | processBatchNotification(request)
        ↓
📦 BatchNotificationService
        |
        | fetchUsers(userIds)
        ↓
💾 Database ────────────── users: [User1, User2, User3]
        |
        | createNotifications(users)
        ↓
📝 Create 3 Notifications ──── saveAll(notifications) ───→ 💾 Database
        |
        ↓
⚙️ Batch Settings Check
        |
        ↓
🚀 Parallel Processing
        |
   ┌────┼────┐
   |    |    |
   ↓    ↓    ↓
📤   📤   📤
Kafka Kafka Kafka
Event1 Event2 Event3
   |    |    |
   ↓    ↓    ↓
📥   📥   📥
Consumer1 Consumer2 Consumer3
   |    |    |
   ↓    ↓    ↓
⚡   ⚡   ⚡
Process1 Process2 Process3
   |    |    |
   ↓    ↓    ↓
📧   📧   📧
Email1  Email2  Email3
   |    |    |
   └────┼────┘
        |
        ↓
📊 Collect Results
        |
        | Success: 3, Failed: 0
        ↓
🎉 200 OK - Batch Completed
```

---

## 👥 **4. User Management Flows**

### Create User:
```
🌐 Client ──POST /users──→ 📋 Controller ──save(user)──→ 💾 Database ──✅ User Created──→ 🎉 201 Created
```

### Get All Users:
```
🌐 Client ──GET /users──→ 📋 Controller ──findAll()──→ 💾 Database ──📋 User List──→ 🎉 200 OK
```

### Get User by ID:
```
🌐 Client ──GET /users/{id}──→ 📋 Controller ──findById(id)──→ 💾 Database
                                                                    |
                                                               ┌────┴────┐
                                                               |         |
                                                            ✅ Found   ❌ Not Found
                                                               |         |
                                                        🎉 200 OK   🚫 404 Not Found
```

---

## ⚙️ **5. Admin Operations**

### System Health Check:
```
🌐 Admin Client ──GET /admin/status──→ 📋 AdminController ──buildSystemStatus()──→ 📊 System Status ──→ 🎉 200 OK
```

### Get Available Channels:
```
🌐 Admin Client ──GET /admin/channels──→ 📋 AdminController ──getAllChannels()──→ 🏭 ChannelFactory ──📡 Channel List──→ 🎉 200 OK
```

### Cancel Scheduled Job:
```
🌐 Admin Client ──POST /admin/scheduler/cancel/{id}──→ 📋 AdminController
                                                            |
                                                            | cancelScheduledNotification(id)
                                                            ↓
                                                      ⏰ QuartzScheduler
                                                            |
                                                            | findActiveJob(notificationId)
                                                            ↓
                                                      💾 Database
                                                            |
                                                       ┌────┴────┐
                                                       |         |
                                                    ✅ Found   ❌ Not Found
                                                       |         |
                                                   🗑️ Delete  📋 No Action
                                                    Job        |
                                                       |         |
                                              💾 Mark as    💾 No Change
                                                Completed      |
                                                       |         |
                                                  ✅ cancelled: true  ❌ cancelled: false
                                                       |         |
                                                       └────┬────┘
                                                            |
                                                            ↓
                                                      🎉 200 OK
```

---

## 🔄 **6. Error Handling & Retry Flow**

```
📥 Kafka Consumer ──processNotification()──→ ⚡ NotificationProcessor
                                                      |
                                                      | send(notification)
                                                      ↓
                                              📡 EmailChannel
                                                      |
                                                      | send email
                                                      ↓
                                              📮 Gmail SMTP
                                                      |
                                                 ┌────┴────┐
                                                 |         |
                                              ✅ SUCCESS ❌ FAILED
                                                 |         |
                                          ✅ Email Sent   📊 Check Retry Count
                                                 |              |
                                          💾 Status: SENT  ┌────┴────┐
                                                           |         |
                                                    < 3 Retries   ≥ 3 Retries
                                                           |         |
                                                  🔄 Retry Topic  💀 Dead Letter Queue
                                                           |         |
                                              ⏰ Wait (exponential  📋 Manual Investigation
                                                   backoff)        Required
                                                           |
                                                           ↓
                                                  📥 Retry Consumer
                                                           |
                                                           ↓
                                              ⚡ NotificationProcessor
                                                      (try again)
```

---

## 🏗️ **7. System Architecture - Component Layout**

```
                                🌐 CLIENT LAYER
    ┌─────────────────┬─────────────────┬─────────────────┐
    |   REST Clients  |     Postman     |   Web Apps      |
    └─────────────────┴─────────────────┴─────────────────┘
                                    |
    ────────────────────────────────┼────────────────────────────────
                                    |
                             🎮 CONTROLLER LAYER
    ┌─────────────────┬─────────────────┬─────────────────┐
    | Notification    |      User       |      Admin      |
    | Controller      |   Controller    |   Controller    |
    └─────────────────┴─────────────────┴─────────────────┘
                                    |
    ────────────────────────────────┼────────────────────────────────
                                    |
                              🏢 SERVICE LAYER
    ┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
    | Notification    |     Batch       | Notification    |   Scheduler     |
    |   Service       |   Service       |  Processor      |   Service       |
    └─────────────────┴─────────────────┴─────────────────┴─────────────────┘
                                    |
    ────────────────────────────────┼────────────────────────────────
                                    |
    ⚡ ASYNC PROCESSING LAYER                     📡 CHANNEL LAYER
    ┌─────────────────┬─────────────────┐       ┌─────────────────┐
    |  Kafka Producer |  Quartz Scheduler|       | Channel Factory |
    |       ↓         |        ↓        |       |       ↓         |
    |  Kafka Topics   |  Notification   |       |   📧 📱 🔔      |
    |       ↓         |     Jobs        |       |  Email SMS Push |
    |  Kafka Consumer |                 |       └─────────────────┘
    └─────────────────┴─────────────────┘
                                    |
    ────────────────────────────────┼────────────────────────────────
                                    |
                              💾 DATA LAYER
                        ┌─────────────────┐
                        |   PostgreSQL    |
                        |                 |
                        | • users         |
                        | • notifications |
                        | • scheduled_jobs|
                        └─────────────────┘
                                    |
    ────────────────────────────────┼────────────────────────────────
                                    |
                            🌍 EXTERNAL SERVICES
                   ┌─────────────────┬─────────────────┐
                   |   Gmail SMTP    |    Twilio SMS   |
                   |   (Real Email)  |   (Mock/Console)|
                   └─────────────────┴─────────────────┘
```

---

## 📊 **8. Key Decision Points**

```
                            📧 Notification Request
                                      |
                                      ↓
                            ❓ Has scheduledAt field?
                               /            \
                            YES              NO
                             |               |
                             ↓               ↓
                    ⏰ SCHEDULED PATH    ⚡ IMMEDIATE PATH
                         |                   |
                         ↓                   ↓
                   Use Quartz           ❓ Kafka Enabled?
                   Scheduler                 |
                         |              /        \
                         ↓           YES          NO
                   Schedule for    |              |
                   future time     ↓              ↓
                         |      Use Kafka    Direct Process
                         |      (Async)     (Synchronous)
                         |         |              |
                         ↓         ↓              ↓
                   ✅ Response   ✅ Response   ✅ Response
                   201 Created   201 Created   201 Created
                                 |              |
                    ⏰ Wait...    ↓              ↓
                         |    📨 Background    📧 Immediate
                         |    Processing      Email Send
                         ↓         |              |
                    📧 Execute     ↓              ↓
                    at time    📧 Email Sent  📧 Email Sent
```

---

## 🎯 **9. Data Flow Summary**

```
INPUT DATA                 PROCESSING                OUTPUT DATA
─────────────             ─────────────             ─────────────

🌐 API Request     ──→     ✅ Validation     ──→     📧 Email Message
👤 User Info       ──→     📝 Entity Create  ──→     📱 SMS Message  
📝 Message Content ──→     🎯 Route Decision ──→     🔔 Push Notification
⏰ Schedule Time   ──→     📡 Channel Select ──→     🔄 API Response
⚙️ Batch Settings  ──→     📤 Delivery Try   ──→     📊 Status Update

                           💾 STORAGE
                           ─────────────
                           👥 users table
                           📧 notifications table  
                           ⏰ scheduled_jobs table
                           📋 audit_logs table
```

---

This format should display perfectly in IntelliJ and any text editor! The ASCII diagrams are clear and easy to follow. 🎉