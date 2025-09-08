# 📱 Notification System - High Level User Flows

## 🎯 **Main User Flows**

### 📧 **1. Notification Flow**
```mermaid
flowchart TD
    USER([👤 User]) --> CHOICE{What do you want?}
    
    CHOICE -->|Send Now| IMMEDIATE[📤 Send Immediate Notification]
    CHOICE -->|Send Later| SCHEDULED[⏰ Schedule Notification] 
    CHOICE -->|Send to Many| BATCH[📦 Send Batch Notifications]
    
    IMMEDIATE --> EMAIL_NOW[📧 Email Sent Immediately]
    SCHEDULED --> EMAIL_LATER[📅 Email Sent at Scheduled Time]
    BATCH --> EMAIL_MULTIPLE[📮 Emails Sent to Multiple Users]
    
    EMAIL_NOW --> SUCCESS[✅ Notification Delivered]
    EMAIL_LATER --> SUCCESS
    EMAIL_MULTIPLE --> SUCCESS

    style USER fill:#e1f5fe
    style SUCCESS fill:#c8e6c9
    style IMMEDIATE fill:#fff3e0
    style SCHEDULED fill:#f3e5f5
    style BATCH fill:#e8f5e8
```

### 👥 **2. User Management Flow**
```mermaid
flowchart TD
    ADMIN([👨‍💼 Admin]) --> USER_ACTION{User Management}
    
    USER_ACTION -->|Create| CREATE_USER[➕ Create New User]
    USER_ACTION -->|View All| LIST_USERS[📋 View All Users]
    USER_ACTION -->|Find One| GET_USER[🔍 Find Specific User]
    
    CREATE_USER --> USER_CREATED[✅ User Created Successfully]
    LIST_USERS --> USER_LIST[📄 List of All Users]
    GET_USER --> USER_DETAILS[👤 User Profile Details]

    style ADMIN fill:#e1f5fe
    style USER_CREATED fill:#c8e6c9
    style USER_LIST fill:#c8e6c9
    style USER_DETAILS fill:#c8e6c9
```

### ⚙️ **3. Admin Flow**
```mermaid
flowchart TD
    ADMIN([🔧 Admin]) --> ADMIN_ACTION{Admin Tasks}
    
    ADMIN_ACTION -->|System Health| CHECK_STATUS[📊 Check System Status]
    ADMIN_ACTION -->|Available Channels| CHECK_CHANNELS[📡 View Available Channels]
    ADMIN_ACTION -->|Scheduler Info| SCHEDULER_INFO[⏰ View Scheduler Details]
    ADMIN_ACTION -->|Cancel Job| CANCEL_JOB[🗑️ Cancel Scheduled Notification]
    
    CHECK_STATUS --> STATUS_REPORT[📈 System Status Report]
    CHECK_CHANNELS --> CHANNEL_LIST[📋 Available Channels List]
    SCHEDULER_INFO --> SCHEDULER_REPORT[📊 Scheduler Information]
    CANCEL_JOB --> JOB_CANCELLED[✅ Job Cancelled Successfully]

    style ADMIN fill:#e1f5fe
    style STATUS_REPORT fill:#c8e6c9
    style CHANNEL_LIST fill:#c8e6c9
    style SCHEDULER_REPORT fill:#c8e6c9
    style JOB_CANCELLED fill:#c8e6c9
```

---

## 🎭 **Complete User Journey**

### 📱 **From Start to Finish**
```mermaid
flowchart TD
    START([🌐 User Opens App/API]) --> LOGIN{Already have users?}
    
    LOGIN -->|No| CREATE_USERS[➕ Create Users First]
    LOGIN -->|Yes| MAIN_MENU[📋 Main Menu]
    
    CREATE_USERS --> USER_CREATED[✅ Users Created]
    USER_CREATED --> MAIN_MENU
    
    MAIN_MENU --> NOTIFICATION_CHOICE{What type of notification?}
    
    NOTIFICATION_CHOICE -->|🚀 Quick & Immediate| SEND_NOW[📤 Send Now]
    NOTIFICATION_CHOICE -->|⏰ Plan for Later| SEND_SCHEDULED[📅 Schedule for Later]
    NOTIFICATION_CHOICE -->|📦 Multiple Recipients| SEND_BATCH[📮 Send to Many Users]
    
    SEND_NOW --> IMMEDIATE_RESULT[⚡ Instant Delivery]
    SEND_SCHEDULED --> SCHEDULED_RESULT[⏳ Scheduled for Future]
    SEND_BATCH --> BATCH_RESULT[📊 Batch Processing Complete]
    
    IMMEDIATE_RESULT --> CHECK_STATUS[📊 Check Notification Status]
    SCHEDULED_RESULT --> CHECK_STATUS
    BATCH_RESULT --> CHECK_STATUS
    
    CHECK_STATUS --> ADMIN_PANEL{Need Admin Tools?}
    ADMIN_PANEL -->|Yes| ADMIN_TASKS[⚙️ System Management]
    ADMIN_PANEL -->|No| DONE[🎉 All Done!]
    
    ADMIN_TASKS --> DONE

    style START fill:#e1f5fe
    style DONE fill:#c8e6c9
    style SEND_NOW fill:#fff3e0
    style SEND_SCHEDULED fill:#f3e5f5
    style SEND_BATCH fill:#e8f5e8
    style ADMIN_TASKS fill:#fce4ec
```

---

## 🎯 **Quick Decision Tree**

### 💭 **"What should I do?"**
```mermaid
flowchart TD
    QUESTION([🤔 What do you want to accomplish?]) --> PURPOSE{Your Goal}
    
    PURPOSE -->|📧 Send notifications to users| NOTIFY_FLOW[Go to Notification Flow]
    PURPOSE -->|👥 Manage user accounts| USER_FLOW[Go to User Management Flow]
    PURPOSE -->|⚙️ System administration| ADMIN_FLOW[Go to Admin Flow]
    
    NOTIFY_FLOW --> NOTIFY_TYPE{When to send?}
    NOTIFY_TYPE -->|Now| IMMEDIATE_BOX[📤 POST /notifications<br/>Immediate Delivery]
    NOTIFY_TYPE -->|Later| SCHEDULED_BOX[⏰ POST /notifications<br/>With scheduledAt]
    NOTIFY_TYPE -->|Multiple Users| BATCH_BOX[📦 POST /notifications/batch<br/>Batch Processing]
    
    USER_FLOW --> USER_TYPE{What user action?}
    USER_TYPE -->|Create| CREATE_BOX[➕ POST /users<br/>Create New User]
    USER_TYPE -->|View All| LIST_BOX[📋 GET /users<br/>List All Users]
    USER_TYPE -->|Find Specific| GET_BOX[🔍 GET /users/{id}<br/>Get User Details]
    
    ADMIN_FLOW --> ADMIN_TYPE{What admin task?}
    ADMIN_TYPE -->|System Health| STATUS_BOX[📊 GET /admin/status<br/>System Health Check]
    ADMIN_TYPE -->|Available Features| CHANNELS_BOX[📡 GET /admin/channels<br/>Available Channels]
    ADMIN_TYPE -->|Scheduler Management| SCHEDULER_BOX[⏰ GET /admin/scheduler/info<br/>Scheduler Details]
    ADMIN_TYPE -->|Cancel Scheduled Job| CANCEL_BOX[🗑️ POST /admin/scheduler/cancel/{id}<br/>Cancel Job]

    style QUESTION fill:#e1f5fe
    style IMMEDIATE_BOX fill:#fff3e0
    style SCHEDULED_BOX fill:#f3e5f5
    style BATCH_BOX fill:#e8f5e8
    style CREATE_BOX fill:#e8f5e8
    style LIST_BOX fill:#e8f5e8
    style GET_BOX fill:#e8f5e8
    style STATUS_BOX fill:#fce4ec
    style CHANNELS_BOX fill:#fce4ec
    style SCHEDULER_BOX fill:#fce4ec
    style CANCEL_BOX fill:#fce4ec
```

---

## 🚀 **Simple Action Flows**

### 📤 **Send Notification (Most Common)**
```mermaid
flowchart LR
    A[👤 User Wants to Send Notification] --> B[📝 Choose Message & Recipient]
    B --> C[⏰ Choose Timing]
    C --> D{When?}
    D -->|Now| E[📤 Send Immediately]
    D -->|Later| F[📅 Schedule for Later]
    E --> G[✅ Email Delivered]
    F --> H[⏳ Waiting for Scheduled Time] --> G

    style A fill:#e1f5fe
    style G fill:#c8e6c9
    style E fill:#fff3e0
    style F fill:#f3e5f5
```

### 👥 **Manage Users**
```mermaid
flowchart LR
    A[👨‍💼 Admin Needs Users] --> B[➕ Create Users]
    B --> C[📋 View User List]
    C --> D[🔍 Find Specific User]
    D --> E[✅ Users Ready for Notifications]

    style A fill:#e1f5fe
    style E fill:#c8e6c9
```

### ⚙️ **System Administration**
```mermaid
flowchart LR
    A[🔧 Admin Monitors System] --> B[📊 Check System Health]
    B --> C[📡 Verify Available Channels]
    C --> D[⏰ Monitor Scheduled Jobs]
    D --> E{Need to Cancel Jobs?}
    E -->|Yes| F[🗑️ Cancel Specific Jobs]
    E -->|No| G[✅ System Running Smoothly]
    F --> G

    style A fill:#e1f5fe
    style G fill:#c8e6c9
```

---

## 📊 **Flow Summary Table**

| 🎯 **User Type** | 📋 **Main Actions** | 🔗 **API Endpoints** | 🎯 **Outcome** |
|------------------|---------------------|----------------------|-----------------|
| **👤 Regular User** | Send notifications | `POST /notifications`<br/>`POST /notifications/batch` | 📧 Emails delivered |
| **👥 User Manager** | Manage user accounts | `POST /users`<br/>`GET /users`<br/>`GET /users/{id}` | 👤 Users ready for notifications |
| **🔧 System Admin** | Monitor & control system | `GET /admin/status`<br/>`GET /admin/channels`<br/>`GET /admin/scheduler/info`<br/>`POST /admin/scheduler/cancel/{id}` | ⚙️ System running smoothly |
| **📊 Data Analyst** | Track notification history | `GET /notifications/{id}`<br/>`GET /notifications/user/{userId}` | 📈 Insights & reports |

---

## 🎉 **Success States**

### ✅ **Happy Path Results**
```mermaid
flowchart TD
    subgraph "💯 Successful Outcomes"
        EMAIL_SENT[📧 Email Successfully Delivered to User]
        BATCH_COMPLETED[📦 All Batch Notifications Sent]
        JOB_SCHEDULED[⏰ Notification Scheduled for Future]
        USER_CREATED[👤 New User Account Created]
        SYSTEM_HEALTHY[🟢 System Status: All Good]
        JOB_CANCELLED[🗑️ Unwanted Job Successfully Cancelled]
    end
    
    EMAIL_SENT --> HAPPY[😊 Users Receive Important Information]
    BATCH_COMPLETED --> HAPPY
    JOB_SCHEDULED --> HAPPY
    USER_CREATED --> READY[🚀 Ready to Send Notifications]
    SYSTEM_HEALTHY --> CONFIDENT[💪 Admin Confident in System]
    JOB_CANCELLED --> CONFIDENT
    
    READY --> HAPPY
    CONFIDENT --> HAPPY
    HAPPY --> SUCCESS[🎉 Mission Accomplished!]

    style SUCCESS fill:#c8e6c9
    style HAPPY fill:#e8f5e8
    style READY fill:#e8f5e8
    style CONFIDENT fill:#e8f5e8
```

---

This high-level flow focuses on **user journeys** and **business outcomes** rather than technical implementation details! 🎯