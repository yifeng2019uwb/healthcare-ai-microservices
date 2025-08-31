# Kafka Implementation Guide - Healthcare AI Microservices

> **⚠️ DRAFT - NOT NEEDED FOR IMMEDIATE WORK** ⚠️
>
> This document contains detailed implementation details for Apache Kafka integration.
> **Focus on other components first** - this can be implemented later when event-driven architecture is needed.

## 🚀 **Overview**

This guide provides comprehensive implementation details for integrating Apache Kafka as an event bus in the Healthcare AI Microservices platform. Kafka enables asynchronous, decoupled communication between services while providing event sourcing capabilities for compliance and audit requirements.

## 🏗️ **Architecture Integration**

### **Kafka Position in System**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Patient Web   │    │  Provider Web   │    │   Admin Portal  │
│    (React)      │    │   (React)       │    │    (React)      │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
         ┌─────────────────────────────────────────────────┐
         │        Spring Cloud Gateway                     │
         │      Route • Rate Limit • Load Balance          │
         └─────────────────┬───────────────────────────────┘
                           │
                    ┌──────▼──────┐
                    │   Internal  │
                    │   Auth      │
                    │  Service    │
                    │(Spring Boot)│
                    │Uses Supabase│
                    │   SDK       │
                    └──────┬──────┘
                           │
    ┌──────────────────────┼──────────────────────────────┐
    │                      │                              │
┌───▼────┐  ┌──────────┐  ┌─────▼──┐  ┌──────────┐
│Patient │  │Provider  │  │   AI   │  │Appointment│
│Service │  │ Service  │  │Service │  │ Service  │
│Spring  │  │ Spring   │  │Spring  │  │  Spring  │
│Boot    │  │ Boot     │  │Boot    │  │   Boot   │
└───┬────┘  └─────┬────┘  └────┬───┘  └─────┬────┘
    │             │            │            │
    └─────────────┼────────────┼────────────┘
                  │            │
         ┌────────▼────────────▼────────────▼────────┐
         │              Kafka Event Bus              │
         │  Topics: patient-events, appointment-     │
         │  events, document-events, audit-events    │
         └────────────────┬──────────────────────────┘
                          │
         ┌────────────────▼─────────────────────────┐
         │              Data Layer                  │
         └────────┬─────────────────────────────────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
┌───▼────┐  ┌────▼────┐  ┌─────▼────┐
│Postgres│  │  Redis  │  │ Supabase │
│(Main DB)│  │ (Cache) │  │ Storage  │
└────────┘  └─────────┘  └──────────┘
```

## 📋 **Kafka Topics & Events**

### **Event Topics Configuration**

| Topic | Events | Purpose | Partitions | Retention | Cleanup Policy |
|-------|--------|---------|------------|-----------|----------------|
| **patient-events** | PatientCreated, PatientUpdated, PatientDeleted, PatientConsentChanged | Patient lifecycle management | 6 | 7 days | delete |
| **appointment-events** | AppointmentBooked, AppointmentCancelled, AppointmentCompleted, AppointmentReminder | Booking workflow management | 8 | 30 days | delete |
| **document-events** | DocumentUploaded, DocumentDeleted, ConsentGiven, DocumentProcessed | File and consent management | 4 | 90 days | delete |
| **audit-events** | DataAccessed, DataModified, LoginAttempt, ComplianceViolation | GDPR and HIPAA compliance | 12 | 365 days | compact |
| **notification-events** | SendEmail, SendSMS, SendPushNotification | Communication management | 4 | 7 days | delete |
| **ai-events** | ModelTrained, PredictionGenerated, InsightCreated, ModelPerformanceUpdated | AI service monitoring | 6 | 180 days | delete |

### **Event Schema Design**

#### **Base Event Schema**
```json
{
  "eventId": "uuid",
  "eventType": "patient.created",
  "timestamp": "2024-01-15T10:30:00Z",
  "source": "patient-service",
  "version": "1.0",
  "data": {},
  "metadata": {
    "correlationId": "uuid",
    "userId": "uuid",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "sessionId": "uuid"
  }
}
```

#### **Patient Events Schema**
```json
{
  "eventId": "uuid",
  "eventType": "patient.created",
  "timestamp": "2024-01-15T10:30:00Z",
  "source": "patient-service",
  "version": "1.0",
  "data": {
    "patientId": "uuid",
    "userId": "uuid",
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-01-01",
    "email": "john.doe@email.com",
    "phone": "+1234567890",
    "insuranceInfo": {
      "provider": "Blue Cross",
      "policyNumber": "BC123456789"
    }
  },
  "metadata": {
    "correlationId": "uuid",
    "userId": "uuid",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0..."
  }
}
```

#### **Appointment Events Schema**
```json
{
  "eventId": "uuid",
  "eventType": "appointment.booked",
  "timestamp": "2024-01-15T10:30:00Z",
  "source": "appointment-service",
  "version": "1.0",
  "data": {
    "appointmentId": "uuid",
    "patientId": "uuid",
    "providerId": "uuid",
    "scheduledAt": "2024-01-20T14:00:00Z",
    "durationMinutes": 30,
    "appointmentType": "CONSULTATION",
    "location": {
      "type": "VIRTUAL",
      "url": "https://meet.healthcare.com/123"
    },
    "notes": "Follow-up consultation"
  },
  "metadata": {
    "correlationId": "uuid",
    "userId": "uuid",
    "ipAddress": "192.168.1.1"
  }
}
```

## 🔧 **Implementation**

### **1. Maven Dependencies**

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

### **2. Kafka Configuration**

#### **Application Properties**
```yaml
# Kafka Configuration
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
      batch-size: 16384
      linger-ms: 1
      buffer-memory: 33554432
    consumer:
      group-id: ${KAFKA_CONSUMER_GROUP:healthcare-ai-group}
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false
      auto-commit-interval: 1000
      session-timeout-ms: 30000
      heartbeat-interval-ms: 10000
      max-poll-records: 500
      max-poll-interval-ms: 300000
```

#### **Kafka Producer Configuration**
```java
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                       StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                       StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

#### **Kafka Consumer Configuration**
```java
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                       StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                       StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
           kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setErrorHandler(new SeekToCurrentErrorHandler());
        return factory;
    }
}
```

### **3. Event Producer Service**

```java
@Service
@Slf4j
public class KafkaEventProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishEvent(String topic, BaseEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = generateEventKey(event);

            ListenableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, key, eventJson);

            future.addCallback(
                result -> log.info("Event published successfully to topic: {}, partition: {}, offset: {}",
                                 topic, result.getRecordMetadata().partition(),
                                 result.getRecordMetadata().offset()),
                ex -> log.error("Failed to publish event to topic: {}", topic, ex)
            );

        } catch (Exception e) {
            log.error("Error publishing event to topic: {}", topic, e);
            throw new EventPublishException("Failed to publish event", e);
        }
    }

    private String generateEventKey(BaseEvent event) {
        // Generate key based on event type and data
        if (event.getData() instanceof PatientEventData) {
            PatientEventData data = (PatientEventData) event.getData();
            return data.getPatientId();
        } else if (event.getData() instanceof AppointmentEventData) {
            AppointmentEventData data = (AppointmentEventData) event.getData();
            return data.getAppointmentId();
        }
        return event.getEventId();
    }
}
```

### **4. Event Consumer Service**

```java
@Service
@Slf4j
public class PatientEventConsumer {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationService notificationService;

    @KafkaListener(
        topics = "${kafka.topics.patient-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePatientEvent(
            ConsumerRecord<String, String> record,
            Acknowledgment ack) {

        try {
            log.info("Received patient event: key={}, partition={}, offset={}",
                    record.key(), record.partition(), record.offset());

            BaseEvent event = parseEvent(record.value());

            switch (event.getEventType()) {
                case "patient.created":
                    handlePatientCreated(event);
                    break;
                case "patient.updated":
                    handlePatientUpdated(event);
                    break;
                case "patient.deleted":
                    handlePatientDeleted(event);
                    break;
                case "patient.consent.changed":
                    handlePatientConsentChanged(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }

            // Log audit event
            auditService.logEvent(event);

            // Acknowledge message
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing patient event: key={}", record.key(), e);
            // Don't acknowledge - message will be retried
        }
    }

    private void handlePatientCreated(BaseEvent event) {
        PatientEventData data = (PatientEventData) event.getData();

        // Update local cache
        patientService.updatePatientCache(data.getPatientId());

        // Send welcome notification
        notificationService.sendWelcomeEmail(data.getEmail(), data.getFirstName());

        // Update analytics
        // analyticsService.recordPatientCreated(data);
    }

    private void handlePatientUpdated(BaseEvent event) {
        PatientEventData data = (PatientEventData) event.getData();

        // Update local cache
        patientService.updatePatientCache(data.getPatientId());

        // Notify relevant services
        notificationService.notifyProviderUpdate(data.getPatientId());
    }

    private BaseEvent parseEvent(String eventJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(eventJson, BaseEvent.class);
    }
}
```

### **5. Event Models**

#### **Base Event Class**
```java
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private String version;
    private EventMetadata metadata;

    @JsonIgnore
    public abstract Object getData();

    @Data
    public static class EventMetadata {
        private String correlationId;
        private String userId;
        private String ipAddress;
        private String userAgent;
        private String sessionId;
    }
}
```

#### **Patient Event Data**
```java
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientEventData {
    private String patientId;
    private String userId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;
    private InsuranceInfo insuranceInfo;
    private List<String> medicalConditions;
    private List<String> allergies;

    @Data
    public static class InsuranceInfo {
        private String provider;
        private String policyNumber;
        private LocalDate effectiveDate;
        private LocalDate expiryDate;
    }
}
```

## 🚀 **Deployment & Infrastructure**

### **1. Docker Compose for Local Development**

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    hostname: zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    volumes:
      - zookeeper-data:/var/lib/zookeeper/data
      - zookeeper-logs:/var/lib/zookeeper/log

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    hostname: kafka
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
      - "9101:9101"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
      KAFKA_DELETE_TOPIC_ENABLE: 'true'
    volumes:
      - kafka-data:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    depends_on:
      - kafka
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
      KAFKA_CLUSTERS_0_ZOOKEEPER: zookeeper:2181

volumes:
  zookeeper-data:
  zookeeper-logs:
  kafka-data:
```

### **2. Topic Creation Scripts**

```bash
#!/bin/bash

# Create Kafka topics
kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --topic patient-events \
    --partitions 6 \
    --replication-factor 1

kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --topic appointment-events \
    --partitions 8 \
    --replication-factor 1

kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --topic document-events \
    --partitions 4 \
    --replication-factor 1

kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --topic audit-events \
    --partitions 12 \
    --replication-factor 1

kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --topic notification-events \
    --partitions 4 \
    --replication-factor 1

kafka-topics.sh --create --bootstrap-server localhost:9092 \
    --topic ai-events \
    --partitions 6 \
    --replication-factor 1
```

## 📊 **Monitoring & Observability**

### **1. Kafka Metrics**

```yaml
# Micrometer Kafka metrics
management:
  metrics:
    export:
      prometheus:
        enabled: true
    kafka:
      producer:
        enabled: true
      consumer:
        enabled: true
```

### **2. Health Checks**

```java
@Component
public class KafkaHealthIndicator implements HealthIndicator {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public Health health() {
        try {
            // Test Kafka connectivity
            kafkaTemplate.send("health-check", "test", "health-check");
            return Health.up()
                .withDetail("kafka", "Available")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("kafka", "Unavailable")
                .withException(e)
                .build();
        }
    }
}
```

## 🔒 **Security & Compliance**

### **1. Event Encryption**

```java
@Component
public class EventEncryptionService {

    @Value("${kafka.encryption.key}")
    private String encryptionKey;

    public String encryptEvent(String eventJson) {
        // Implement AES encryption for sensitive events
        // Required for HIPAA compliance
        return encryptAES(eventJson, encryptionKey);
    }

    public String decryptEvent(String encryptedEvent) {
        return decryptAES(encryptedEvent, encryptionKey);
    }
}
```

### **2. Audit Trail**

```java
@Service
public class EventAuditService {

    @Autowired
    private AuditRepository auditRepository;

    public void logEvent(BaseEvent event) {
        EventAudit audit = EventAudit.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .timestamp(event.getTimestamp())
            .source(event.getSource())
            .userId(event.getMetadata().getUserId())
            .ipAddress(event.getMetadata().getIpAddress())
            .build();

        auditRepository.save(audit);
    }
}
```

## 🎯 **Best Practices**

### **1. Event Design**
- Use descriptive event names (e.g., `patient.consent.changed`)
- Include correlation IDs for tracing
- Version your events for backward compatibility
- Keep events focused and single-purpose

### **2. Error Handling**
- Implement dead letter queues for failed events
- Use exponential backoff for retries
- Log all event processing failures
- Monitor consumer lag and performance

### **3. Performance**
- Use appropriate partition counts for topics
- Implement batch processing for high-volume events
- Monitor producer and consumer performance
- Use compression for large events

### **4. Security**
- Encrypt sensitive event data
- Implement proper authentication and authorization
- Use secure network connections (TLS)
- Monitor for suspicious event patterns

---

## 📋 **Implementation Priority**

> **🔴 LOW PRIORITY - IMPLEMENT LATER**
>
> This Kafka implementation is **not needed for the initial phases** of the project.
>
> **Focus on these components first:**
> 1. ✅ **Auth Service** - Authentication and authorization
> 2. ✅ **Gateway Service** - API routing and security
> 3. ✅ **Core Services** - Patient, Provider, Appointment services
> 4. ✅ **Database Layer** - PostgreSQL schema and data access
> 5. 🔄 **Kafka Event Bus** - Event-driven communication (Phase 3+)

---

*This guide provides the foundation for implementing a robust, scalable event-driven architecture using Apache Kafka in your Healthcare AI Microservices platform.*
