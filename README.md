# Notification System

A scalable, extensible notification system built with Spring Boot that supports multiple notification channels and scheduling capabilities.

## Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Maven 3.8+
- Apache Kafka (via Homebrew)

### Setup Database
```sql
CREATE DATABASE notification_db;
CREATE USER postgres WITH ENCRYPTED PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE notification_db TO postgres;
```

### Install Dependencies (macOS with Homebrew)
```bash
# Install PostgreSQL
brew install postgresql
brew services start postgresql

# Install Kafka
brew install kafka
brew services start kafka

# Verify services are running
brew services list | grep -E "(postgresql|kafka)"
```

### Run Application

#### Option 1: Automated Startup (Recommended)
Use the provided startup scripts that automatically ensure all dependencies are running:

```bash
# Simple startup - automatically starts PostgreSQL and optionally Kafka
./start-simple.sh

# Advanced startup - comprehensive dependency management with health checks
./start-app.sh
```

#### Option 2: Manual Startup
```bash
# Make sure PostgreSQL is running
brew services start postgresql

# Create database if it doesn't exist
createdb -U sandeepkumaryadav notification_db

# Optional: Start Kafka (if not already running)
brew services start kafka

# Start the application
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/notification-system-1.0.0-SNAPSHOT.jar
```

#### Option 3: JAR File Execution
```bash
# Build the JAR file
mvn clean package -DskipTests

# Run the JAR in foreground
java -jar target/notification-system-1.0.0-SNAPSHOT.jar

```

### Stop Application
```bash
# If running with mvn spring-boot:run, use Ctrl+C to stop

# Or find PID and send SIGTERM
PID=$(jps -v | grep notification-system | awk '{print $1}')
kill -TERM $PID

# Force kill if unresponsive
pkill -f "notification-system"
```

### API Documentation
Once running, visit: http://localhost:8080/swagger-ui/index.html

### Test the APIs

#### 1. Create a Test User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "phoneNumber": "+1234567890"
  }'
```

#### 2. Send an Immediate Notification
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Welcome!",
    "content": "Welcome to our notification system!",
    "channelType": "EMAIL",
    "priority": "HIGH"
  }'
```

#### 3. Send a Scheduled Notification
```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "title": "Scheduled Reminder",
    "content": "This is a scheduled notification",
    "channelType": "EMAIL",
    "priority": "MEDIUM",
    "scheduledAt": "2024-12-25T10:00:00"
  }'
```

#### 4. Check Notification Status
```bash
curl http://localhost:8080/api/v1/notifications/1
```

#### 5. Send Batch Notifications
```bash
curl -X POST http://localhost:8080/api/v1/notifications/batch \
  -H "Content-Type: application/json" \
  -d '{
    "userIds": [1, 2, 3],
    "title": "System Maintenance Notice",
    "content": "System will undergo maintenance from 2:00 AM to 4:00 AM EST",
    "channelType": "EMAIL",
    "priority": "HIGH",
    "batchSettings": {
      "batchSize": 10,
      "delayBetweenBatches": 1000,
      "parallelProcessing": true,
      "continueOnError": true
    }
  }'
```

#### 6. Get User's Notifications
```bash
curl http://localhost:8080/api/v1/notifications/user/1?page=0&size=10
```

## Architecture Highlights

- **Modular Monolith**: Easy to develop and deploy, with clear module boundaries
- **Event-Driven**: Asynchronous processing for scalability
- **Extensible Channels**: Strategy pattern for easy addition of new notification types
- **Priority-Based Processing**: High, Medium, Low priority handling
- **Retry Mechanism**: Exponential backoff for failed notifications
- **Scheduled Notifications**: Support for future and recurring notifications
- **Batch Processing**: Configurable batch processing with parallel execution
- **Real Channel Support**: SMTP Email integration with HTML formatting

## Technology Stack

- **Framework**: Spring Boot 3.2.x
- **Database**: PostgreSQL + JPA/Hibernate
- **Security**: Spring Security with JWT
- **Scheduler**: Quartz
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5