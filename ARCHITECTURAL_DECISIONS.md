# Notification System - Architectural Decisions & Planning

## Project Overview
Building a scalable, extensible notification system in Java that supports:
- Multi-channel notifications (Email, SMS, Push, extensible to WhatsApp/Slack)
- User management with personalized notifications
- Scheduling (immediate + future + recurring notifications)
- Priority levels (high/medium/low) with batching
- Failure handling with exponential backoff retry logic
- Handle millions of notifications across regions
- Low latency for real-time, efficient batching for scheduled
- Fault tolerant with eventual delivery guarantee

---

## Planning Discussion Log

### Session 1 - Initial Architecture Discussion

#### 1. Overall Architecture Pattern
**Options Considered:**
- **Microservices** (separate services for scheduling, delivery, user management)
- **Modular Monolith** (single deployable with clear module boundaries)
- **Event-Driven Architecture** with message queues

**Claude's Recommendation**: **Modular Monolith with Event-Driven components**

**Justification:**
- Simpler to develop and test initially
- Clear module boundaries make it easy to extract microservices later
- Event-driven for async processing of notifications
- Reduces operational complexity while maintaining scalability options

**Status**: Pending user feedback

#### 2. Technology Stack Discussion

**Core Framework**: Spring Boot
- **Justification**: Industry standard, excellent ecosystem, mature tooling

**Build Tool Options**: Maven vs Gradle
- **Claude's lean**: Maven for enterprise projects (better IDE support, more standardized)

**Questions for Decision:**
1. Spring Boot version preference? (Latest stable is 3.2.x)
2. Include Spring Security from start for API authentication?
3. Java version requirements? (Recommendation: Java 17+ for modern features)

**Status**: Pending user feedback

#### 3. Database Strategy

**Primary Database Options Evaluated:**

**PostgreSQL** ✅ (Recommended)
- **Pros**: Excellent JSON support, ACID compliance, complex queries, proven scale
- **Cons**: Slightly more resource intensive than MySQL
- **Use Case**: Primary storage for users, notifications, scheduling

**MySQL**
- **Pros**: Proven scale, good replication, lighter weight
- **Cons**: Less flexible JSON support
- **Use Case**: Alternative to PostgreSQL

**MongoDB**
- **Pros**: Document-based, natural fit for varied notification payloads
- **Cons**: Less mature tooling for complex queries, eventual consistency challenges
- **Use Case**: Could work for notification content storage

**Caching & Performance Layer:**

**Redis** ✅ (Recommended)
- **Use Cases**: 
  - User preference caching
  - Rate limiting
  - Session management
  - Queue management for high-priority notifications

**Time-Series Considerations:**
- **InfluxDB/TimescaleDB**: For notification metrics and history
- **Justification**: Better performance for time-based analytics and reporting

**Claude's Initial Recommendation**: **PostgreSQL as primary + Redis for caching**
- PostgreSQL handles complex scheduling queries well
- Redis for high-speed user preference lookups and rate limiting
- Clean separation of concerns

**Status**: Pending user feedback

---

## Next Discussion Topics
1. User feedback on architecture pattern choice
2. Technology stack finalization
3. Database strategy confirmation
4. Message Queue/Event System design
5. Scheduler implementation approach
6. Channel extensibility architecture
7. Retry and failure handling strategy
8. API design patterns
9. Testing strategy
10. Deployment and monitoring approach

---

## Decisions Made

### ✅ Architecture Pattern: Modular Monolith with Event-Driven Components
**User Decision**: Approved

**Options Considered**:
1. **Modular Monolith with Event-Driven Components** ✅ (Selected)
   - Simpler to develop and test initially
   - Clear module boundaries make it easy to extract microservices later
   - Event-driven for async processing of notifications
   - Reduces operational complexity while maintaining scalability options
   - Single deployment unit reduces DevOps complexity

2. **Microservices** ❌
   - Independent scaling of services
   - Technology diversity per service
   - **Rejected because**: Increased operational complexity, network latency, distributed system challenges, premature for initial implementation

3. **Traditional Monolith** ❌  
   - Simplest to start with
   - **Rejected because**: Harder to scale, tightly coupled components, difficult to extract services later

### ✅ Technology Stack
**Core Framework**: Spring Boot 3.2.x (latest stable)
**Build Tool**: Maven 
**Java Version**: Java 17+
**Security**: Spring Security included from start
**User Decision**: All approved

### ✅ Database Strategy
**User Decision**: Approved PostgreSQL + Redis + InfluxDB approach

**Options Considered**:

**Primary Database Options**:
1. **PostgreSQL** ✅ (Selected)
   - Excellent JSON support for varied notification payloads
   - ACID compliance for reliable transactions
   - Complex scheduling queries with advanced SQL features
   - Proven scalability for millions of records
   - Strong Spring Data JPA integration

2. **MySQL** ❌
   - Proven scale and good replication
   - Lighter weight than PostgreSQL
   - **Rejected because**: Less flexible JSON support, weaker complex query capabilities

3. **MongoDB** ❌
   - Document-based, natural fit for varied notification payloads
   - Good horizontal scaling
   - **Rejected because**: Less mature tooling for complex scheduling queries, eventual consistency challenges for critical notifications

**Caching Layer**:
- **Redis** ✅ (Selected): User preference caching, rate limiting, session management, queue management for high-priority notifications

**Analytics/Metrics**:
- **InfluxDB** ✅ (Selected): Time-series data for notification metrics and history, better performance for time-based analytics

### ✅ Message Queue/Event System: Apache Kafka
**User Decision**: Approved

**Options Considered**:
1. **Apache Kafka** ✅ (Selected)
   - Exceptional throughput (millions of messages/second)
   - Built-in partitioning for scaling
   - Message persistence and replay capability
   - Perfect for event-driven architecture
   - Strong ordering guarantees within partitions

2. **RabbitMQ** ❌
   - Excellent routing flexibility
   - Built-in retry mechanisms
   - Good Spring integration
   - **Rejected because**: Lower throughput compared to Kafka, more complex clustering

3. **Redis Streams** ❌
   - We're already using Redis for caching
   - Good for moderate scale
   - **Rejected because**: Not designed for millions of messages, less mature ecosystem

**Topic Structure**:
- `high-priority-notifications` - Immediate processing
- `scheduled-notifications` - Future delivery
- `retry-notifications` - Failed notification retries
- `notification-events` - Analytics and monitoring

### ✅ Scheduler Implementation: Quartz Scheduler
**User Decision**: Approved

**Options Considered**:
1. **Quartz Scheduler** ✅ (Selected)
   - Industry standard for Java scheduling
   - Database-backed persistence (PostgreSQL integration)
   - Cluster support for high availability
   - Supports both cron-like and programmatic scheduling
   - Perfect for scheduled and recurring notifications

2. **Spring @Scheduled** ❌
   - Simple implementation
   - Good for basic use cases
   - **Rejected because**: Limited scalability, no cluster support, not suitable for millions of scheduled notifications

3. **Custom Event-Driven Scheduler** ❌
   - Fully customizable
   - Uses Kafka delayed messages
   - **Rejected because**: More complex to implement and maintain, reinventing the wheel when Quartz is mature and proven

**Integration**: Quartz for scheduling + Kafka for immediate processing pipeline

### ✅ Channel Architecture Pattern: Strategy Pattern + Factory
**User Decision**: Approved

**Options Considered**:
1. **Strategy Pattern + Factory** ✅ (Selected)
   - Easy to add new channels without modifying existing code
   - Clean separation of concerns per channel
   - Testable in isolation
   - Follows Open/Closed principle
   - Runtime channel selection based on user preferences

2. **Plugin Architecture** ❌
   - Dynamic loading of channel implementations
   - Hot-swapping of channels
   - **Rejected because**: Added complexity for dynamic loading, potential security concerns, overkill for our use case

3. **Hard-coded Channel Selection** ❌
   - Simple if-else or switch statements
   - **Rejected because**: Violates Open/Closed principle, requires code changes for new channels, not extensible

**Implementation Structure**:
```java
public interface NotificationChannel {
    NotificationResult send(NotificationRequest request);
    boolean supports(ChannelType channelType);
    ChannelType getChannelType();
}
```

### ✅ Retry Mechanism: Exponential Backoff with Circuit Breaker
**User Decision**: Approved

**Options Considered**:
1. **Exponential Backoff with Circuit Breaker** ✅ (Selected)
   - Graceful handling of transient failures
   - Prevents system overload during outages
   - Configurable retry limits and backoff multipliers
   - Dead letter queue for permanent failures
   - Channel-specific circuit breakers

2. **Fixed Interval Retry** ❌
   - Simple implementation
   - Predictable retry timing
   - **Rejected because**: Can overwhelm failing services, doesn't adapt to failure patterns

3. **Linear Backoff** ❌
   - Moderate increase in retry intervals
   - **Rejected because**: Not as effective as exponential for handling load spikes

**Retry Strategy**:
- Initial retry: 1 second
- Exponential backoff: 1s → 2s → 4s → 8s → 16s → 32s
- Max attempts: 6 retries over ~1 hour
- Circuit breaker: Stop retrying if channel consistently fails
- Dead letter queue: Permanently failed notifications for manual review

### ✅ API Design Patterns: RESTful API with Resource-Based Design
**User Decision**: Approved

**Options Considered:**
1. **RESTful API with Resource-Based Design** ✅ (Selected)
   - Industry standard, well understood
   - Clear HTTP verb semantics (GET, POST, PUT, DELETE)
   - Easy to document and test
   - Excellent Spring Boot integration
   - Support for pagination, filtering, sorting

2. **GraphQL** ❌
   - Flexible queries, reduced over-fetching
   - Single endpoint design
   - **Rejected because**: Added complexity, overkill for notification system, steeper learning curve, less familiar to most developers

3. **RPC-style API** ❌
   - Simple method calls
   - Direct function mapping
   - **Rejected because**: Less discoverable, doesn't leverage HTTP semantics, harder to cache

**API Structure:**
```
POST   /api/v1/notifications              # Send immediate notification
POST   /api/v1/notifications/scheduled    # Schedule notification
POST   /api/v1/notifications/bulk         # Bulk notifications
GET    /api/v1/notifications/{id}         # Get notification status
PUT    /api/v1/notifications/{id}/cancel  # Cancel scheduled notification

GET    /api/v1/users/{id}/preferences     # User notification preferences
PUT    /api/v1/users/{id}/preferences     # Update preferences

GET    /api/v1/notifications/history      # Notification history with pagination
```

### ✅ Authentication & Authorization: JWT with Spring Security
**User Decision**: Approved

**Options Considered:**
1. **JWT with Spring Security** ✅ (Selected)
   - Stateless authentication
   - Good for API-first design
   - Easy to implement with Spring Security
   - Scalable across multiple instances
   - No server-side session storage needed

2. **OAuth2 with External Provider** ❌
   - Industry standard for external integrations
   - Delegated authentication
   - **Rejected because**: Added complexity, external dependency, overkill for notification system

3. **API Keys Only** ❌
   - Simple for service-to-service communication
   - **Rejected because**: Less secure, harder to manage user-specific permissions, no user context

**Security Features:**
- JWT tokens for authentication
- Role-based access control (ADMIN, USER)
- Rate limiting per user/API key
- Request validation and sanitization

### ✅ Module Structure & Boundaries: Layered Architecture with Domain Separation
**User Decision**: Approved

**Complete Package Structure:**
```
com.notification.system/
├── controller/            # REST Controllers (API Layer)
│   ├── NotificationController
│   ├── UserController
│   ├── SchedulingController
│   └── AdminController
├── service/              # Business Logic Layer
│   ├── notification/     # Notification processing
│   ├── scheduling/       # Scheduling logic
│   ├── channel/         # Channel implementations
│   ├── user/            # User management
│   └── retry/           # Retry logic
├── repository/          # Data Access Layer
│   ├── NotificationRepository
│   ├── UserRepository
│   └── ScheduledJobRepository
├── model/              # Domain Models & Entities
│   ├── entity/         # JPA Entities
│   │   ├── User
│   │   ├── Notification
│   │   └── ScheduledJob
│   ├── dto/           # Data Transfer Objects
│   │   ├── request/   # API Request DTOs
│   │   └── response/  # API Response DTOs
│   └── enums/         # Enums (Priority, ChannelType, Status)
├── config/            # Configuration Classes
│   ├── DatabaseConfig
│   ├── KafkaConfig
│   ├── SecurityConfig
│   └── QuartzConfig
├── event/            # Event Handling
│   ├── publisher/    # Event publishers
│   └── listener/     # Event listeners
├── exception/        # Custom Exceptions & Error Handling
└── common/           # Shared Utilities
    ├── constants/    # Application constants
    └── utils/        # Utility classes
```

### ✅ Rate Limiting Approach: Redis-based Distributed Rate Limiting
**User Decision**: Approved

**Options Considered:**
1. **Redis-based Rate Limiting** ✅ (Selected)
   - Uses Redis counters with TTL expiration
   - Distributed across multiple instances
   - Scalable and consistent
   - Integration with existing Redis infrastructure
   - Per-user and per-API-key granular limits

2. **In-memory Rate Limiting (Bucket4j)** ❌
   - Simple implementation
   - Good performance for single instance
   - **Rejected because**: Doesn't work across multiple instances, limits not shared, inconsistent behavior in distributed setup

3. **Database-based Rate Limiting** ❌
   - Persistent rate limit data
   - **Rejected because**: Higher latency, database overhead, not suitable for high-frequency checks

**Rate Limiting Strategy:**
- **Regular Users**: 1000 requests/hour for standard APIs
- **Bulk Operations**: 100 requests/hour for bulk notification APIs
- **Admin Users**: 10000 requests/hour for administrative operations
- **Per-API-Key**: Configurable limits for service-to-service communication
- **Burst allowance**: 10% burst capacity for short spikes

### ✅ Monitoring & Observability: Comprehensive Observability Stack
**User Decision**: Approved

**Options Considered:**
1. **Spring Boot Actuator + Micrometer + Prometheus** ✅ (Selected)
   - Industry standard for Spring applications
   - Rich metrics collection out-of-the-box
   - Easy integration with monitoring dashboards
   - Excellent Spring Boot integration

2. **Custom Metrics with InfluxDB only** ❌
   - Direct time-series storage
   - **Rejected because**: Less standardized, missing application-level metrics, more custom development

3. **APM Solutions (New Relic, DataDog)** ❌
   - Comprehensive monitoring solution
   - **Rejected because**: External dependency, cost considerations, overkill for assignment scope

**Observability Stack:**
- **Metrics**: Micrometer + Prometheus (application metrics)
- **Health Checks**: Spring Boot Actuator endpoints
- **Time-Series**: InfluxDB (notification analytics, delivery metrics)
- **Logging**: Structured JSON logging with correlation IDs
- **Monitoring**: Custom dashboards for notification success rates, latency, queue depths

**Key Metrics to Track:**
- Notification delivery success rate by channel
- Average processing time per priority level
- Queue depths across Kafka topics
- Retry attempt distributions
- Channel-specific error rates
- User engagement metrics

## Implementation Planning

### ✅ Implementation Phases: Fast-Track Development Strategy
**Phase 1 - MVP Core** (Day 1-2)
- [ ] Project setup: Spring Boot + Maven + PostgreSQL + basic config
- [ ] Core entities: User, Notification, basic repository layer
- [ ] Simple REST APIs: send notification, get status
- [ ] Basic email channel implementation (mock/console for demo)

**Phase 2 - Essential Features** (Day 3-4)
- [ ] Channel architecture: Strategy pattern + Email/SMS channels
- [ ] Scheduling: Simple Quartz integration for future notifications
- [ ] Priority handling: High/Medium/Low processing
- [ ] Basic retry mechanism (without circuit breaker initially)

**Phase 3 - Polish & Demo-Ready** (Day 5-6)
- [ ] JWT authentication (basic implementation)
- [ ] Unit tests for core functionality
- [ ] API documentation (Swagger)
- [ ] Error handling and validation
- [ ] Demo data and examples

**Optional Enhancements** (If time permits):
- Redis caching layer
- Kafka integration (can use simple queues initially)
- Advanced monitoring
- Bulk operations

## Final Architecture Summary

**✅ All Major Decisions Finalized:**
1. **Architecture**: Modular Monolith with Event-Driven Components
2. **Technology Stack**: Spring Boot 3.2.x, Java 17+, Maven, Spring Security
3. **Databases**: PostgreSQL + Redis + InfluxDB
4. **Messaging**: Apache Kafka with topic-based organization
5. **Scheduling**: Quartz Scheduler with database persistence
6. **Channel Pattern**: Strategy Pattern + Factory for extensibility
7. **Retry Strategy**: Exponential Backoff with Circuit Breaker
8. **API Design**: RESTful with resource-based endpoints
9. **Security**: JWT with Spring Security, role-based access
10. **Module Structure**: Layered architecture with clear boundaries
11. **Rate Limiting**: Redis-based distributed limiting
12. **Observability**: Spring Actuator + Micrometer + Prometheus + InfluxDB

**Ready for implementation!** 🚀

---

## Implementation Progress Updates

### ✅ Batch Notification System Implementation (Phase 2.1)
**Implementation Date**: December 2024

#### Batch Processing Architecture Decision
**Problem**: Need to efficiently send notifications to multiple users simultaneously while maintaining control over resource usage and error handling.

**Solution**: Implemented comprehensive batch notification system with configurable processing options.

**Architecture Components**:

1. **BatchNotificationService** - Core batch processing engine
   - Supports both parallel and sequential processing modes
   - Configurable batch sizes and processing delays
   - Advanced error handling with continue-on-error option
   - Detailed statistics and reporting

2. **Batch Processing Strategies**:
   - **Sequential Processing**: Process batches one after another with configurable delays
   - **Parallel Processing**: Use CompletableFuture for concurrent batch processing
   - **Hybrid Approach**: Configurable per-request based on user requirements

3. **Configuration Options**:
   ```json
   {
     "batchSize": 10,              // Users per batch
     "delayBetweenBatches": 1000,  // Milliseconds between batches
     "parallelProcessing": true,   // Enable parallel processing
     "continueOnError": true       // Continue processing on individual failures
   }
   ```

4. **Response Analytics**:
   - Batch ID for tracking
   - Individual notification results
   - Success/failure statistics
   - Processing time metrics
   - Error categorization and breakdown

**API Endpoint**: `POST /api/v1/notifications/batch`

**Key Benefits**:
- ✅ Prevents system overload with configurable batching
- ✅ Provides detailed feedback on processing results
- ✅ Supports both immediate and scheduled batch processing
- ✅ Maintains individual notification tracking within batches
- ✅ Graceful error handling with detailed error reporting

#### SMTP Email Integration Enhancement
**Implementation**: Real Gmail SMTP integration with professional HTML email templates
- Beautiful HTML email formatting with inline CSS styling
- Support for both console debugging and real SMTP delivery
- Channel priority system (@Order annotations) for proper fallback
- Professional email templates with metadata support

---

*This document will be updated as we progress through our architectural discussions.*