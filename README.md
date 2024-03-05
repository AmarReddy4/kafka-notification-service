# Kafka Notification Service

A Spring Boot app that demonstrates async event-driven architecture with Apache Kafka. Consumes user events from Kafka topics and dispatches notifications (email/SMS/push) via a producer-consumer pipeline.

## Architecture

```
[REST API] --> [EventProducer] --> [user-events topic]
                                        |
                                        v
                                [NotificationConsumer]
                                        |
                              [NotificationRouter]
                                        |
                                        v
                              [notifications topic]
                                        |
                                        v
                              [NotificationSender]
                                  (simulated send)
```

Failed messages are automatically retried (3 attempts) and then sent to the `notification-dlq` dead letter queue topic.

## Tech Stack

- Java 17
- Spring Boot 3.2.2
- Spring Kafka
- Apache Kafka (Confluent 7.5.x)
- Maven
- Docker / Docker Compose

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose

### Start Kafka

```bash
docker-compose up -d
```

### Build and Run

```bash
mvn clean package
mvn spring-boot:run
```

### API Endpoints

**Publish an event:**

```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "eventType": "SIGNUP",
    "payload": "New user registration"
  }'
```

**List all sent notifications:**

```bash
curl http://localhost:8080/api/notifications
```

**List notifications for a specific user:**

```bash
curl http://localhost:8080/api/notifications/user-123
```

### Event Types

| Event Type     | Notification Channel | Description             |
|----------------|----------------------|-------------------------|
| SIGNUP         | EMAIL                | Welcome email           |
| PURCHASE       | EMAIL                | Purchase confirmation   |
| PASSWORD_RESET | SMS                  | Password reset code     |

## Topics

- `user-events` - Incoming user events (3 partitions)
- `notifications` - Generated notifications ready to send (3 partitions)
- `notification-dlq` - Dead letter queue for failed messages (1 partition)
