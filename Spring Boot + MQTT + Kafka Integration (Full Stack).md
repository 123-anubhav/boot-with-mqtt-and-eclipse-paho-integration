# boot with mqtt and kafka integration

building a modular, real-world VTS platform, and integrating **Spring Boot + MQTT + Kafka** is a brilliant move for scalable, event-driven architecture. Let’s break this down into two parts:

---

## 🧩 Part 1: Spring Boot + MQTT + Kafka Integration (Full Stack)

### 🎯 Why Kafka with MQTT?

- MQTT is great for **device-to-cloud** communication (GPS, alerts, CMS updates).
- Kafka excels at **cloud-side processing**, **streaming**, **durability**, and **scalability**.
- Together: MQTT → Kafka → Stream processing → Dashboards, alerts, analytics.

---

### 🛠️ Step-by-Step Integration Plan

#### **1. MQTT Ingestion Microservice (Spring Boot)**

This service subscribes to MQTT topics and forwards messages to Kafka.

**Dependencies:**

```xml
<dependency>
  <groupId>org.springframework.integration</groupId>
  <artifactId>spring-integration-mqtt</artifactId>
  <version>5.5.2</version>
</dependency>
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>
```

**application.yml:**

```yaml
mqtt:
  broker: tcp://localhost:1883
  clientId: vts-mqtt-client
  username: yourUser
  password: yourPass
  topic: vehicle/+/location

spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

**MQTT → Kafka Bridge Flow:**

```java
@Configuration
public class MqttKafkaBridge {

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("yourUser");
        options.setPassword("yourPass".toCharArray());

        MqttPahoMessageDrivenChannelAdapter adapter =
            new MqttPahoMessageDrivenChannelAdapter("tcp://localhost:1883", "vts-mqtt-client", "vehicle/+/location");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setConnectOptions(options);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(KafkaTemplate<String, String> kafkaTemplate) {
        return message -> {
            String topic = "vts.location";
            String payload = message.getPayload().toString();
            kafkaTemplate.send(topic, payload);
        };
    }
}
```

---

#### **2. Kafka Consumer Microservice (Spring Boot)**

This service consumes Kafka messages and processes them (e.g., store in DB, trigger alerts, update dashboard).

**Kafka Listener:**

```java
@Service
public class LocationConsumer {

    @KafkaListener(topics = "vts.location", groupId = "vts-group")
    public void consume(String payload) {
        // parse JSON, store, alert, etc.
        System.out.println("Received location: " + payload);
    }
}
```

---

#### **3. Docker Compose Setup**

```yaml
version: '3.8'
services:
  mosquitto:
    image: eclipse-mosquitto
    ports:
      - "1883:1883"
    volumes:
      - ./mosquitto/config:/mosquitto/config

  kafka:
    image: bitnami/kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_CFG_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092
      KAFKA_CFG_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092

  zookeeper:
    image: bitnami/zookeeper
    ports:
      - "2181:2181"
```

---

## 🧪 Part 2: Lightweight Microservice with Raw Paho + Kafka Producer

This is ideal for edge devices or GPS simulators.

**Dependencies:**

```xml
<dependency>
  <groupId>org.eclipse.paho</groupId>
  <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
</dependency>
<dependency>
  <groupId>org.apache.kafka</groupId>
  <artifactId>kafka-clients</artifactId>
</dependency>
```

**MQTT Subscriber → Kafka Producer:**

```java
public class MqttKafkaBridge {

    public static void main(String[] args) throws Exception {
        MqttClient mqttClient = new MqttClient("tcp://localhost:1883", "gps-simulator");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("yourUser");
        options.setPassword("yourPass".toCharArray());
        mqttClient.connect(options);

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        mqttClient.subscribe("vehicle/+/location", (topic, msg) -> {
            String payload = new String(msg.getPayload());
            producer.send(new ProducerRecord<>("vts.location", payload));
        });
    }
}
```

---

## 🧠 Summary

| Layer                | Tech Used                     | Role                                  |
|---------------------|-------------------------------|---------------------------------------|
| Device/Simulator     | Paho MQTT                     | Publish GPS/location events           |
| Ingestion Service    | Spring Boot + MQTT + Kafka    | Subscribe MQTT → Forward to Kafka     |
| Processing Service   | Spring Boot + Kafka Consumer  | Consume Kafka → Store, alert, update  |

---

Ready to scaffold this into a Git-style modular repo with Docker Compose, test payloads, and monitoring? I can wire up the whole flow with real topics like `vehicle/123/location`, `vehicle/123/alert`, and `cms/display/kanpur`. Let’s build it out.
