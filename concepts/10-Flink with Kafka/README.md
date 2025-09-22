# Flink with Kafka: A Detailed Guide for Java Developers

***

## 1. Kafka Connector in Flink

***

### 1.1 FlinkKafkaConsumer and FlinkKafkaProducer Setup

Flink provides connectors to consume and produce data directly from/to Kafka using `FlinkKafkaConsumer` and `FlinkKafkaProducer` classes.

#### Kafka Consumer Setup (Reading from Kafka)

```java
Properties properties = new Properties();
properties.setProperty("bootstrap.servers", "localhost:9092");
properties.setProperty("group.id", "flink-group");

FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
        "input-topic",
        new SimpleStringSchema(),
        properties
);
```

- `bootstrap.servers` specifies Kafka brokers.
- `group.id` assigns consumer group.
- `SimpleStringSchema` deserializes Kafka byte data as strings.
- The constructor links the Kafka topic `input-topic`.

#### Kafka Producer Setup (Writing to Kafka)

```java
FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<>(
        "output-topic",
        new SimpleStringSchema(),
        properties
);
```

- Same properties configure the producer.
- The topic `output-topic` is where transformed data is sent.
- `SimpleStringSchema` serializes strings to Kafka.

***

### 1.2 Kafka Connector Maven Dependencies

Add these dependencies in your **`pom.xml`**:

```xml
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-connector-kafka_2.12</artifactId>
    <version>1.17.1</version>
</dependency>

<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-streaming-java_2.12</artifactId>
    <version>1.17.1</version>
</dependency>
```

Adjust Scala and Flink versions as per your setup.

***

## 2. Kafka → Flink → Kafka Java Job

***

### 2.1 Read Data from Kafka Topic

Example Java Flink job snippet to read from Kafka topic `"input-topic"`:

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

Properties props = new Properties();
props.setProperty("bootstrap.servers", "localhost:9092");
props.setProperty("group.id", "flink-consumer-group");

FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
        "input-topic",
        new SimpleStringSchema(),
        props
);

DataStream<String> stream = env.addSource(kafkaConsumer);
```

***

### 2.2 Transform the Stream

Apply a transformation such as converting strings to uppercase:

```java
DataStream<String> transformedStream = stream.map(String::toUpperCase);
```

***

### 2.3 Write to New Kafka Topic

Create a Kafka producer and sink the transformed result:

```java
FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>(
        "output-topic",
        new SimpleStringSchema(),
        props
);

transformedStream.addSink(kafkaProducer);
```

***

### 2.4 Execute the Flink Job

```java
env.execute("Flink Kafka Transformation Job");
```

***

## 3. Validating Output Using Kafka UI

- Use Kafka management UIs like **Kafka Manager**, **Conduktor**, **AKHQ**, or **Kafka Tool**.
- Connect to your Kafka brokers.
- Watch the `"output-topic"` for transformed data appearing in real-time.
- Verify messages are as expected (e.g., uppercase strings).

***

## Example Full Java Flink Kafka Job

```java
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;

import java.util.Properties;

public class FlinkKafkaStreamJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "flink-consumer-group");

        FlinkKafkaConsumer<String> kafkaConsumer = new FlinkKafkaConsumer<>(
                "input-topic",
                new SimpleStringSchema(),
                props
        );

        DataStream<String> inputStream = env.addSource(kafkaConsumer);

        DataStream<String> transformedStream = inputStream.map(String::toUpperCase);

        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>(
                "output-topic",
                new SimpleStringSchema(),
                props
        );

        transformedStream.addSink(kafkaProducer);

        env.execute("Flink Kafka Transformation Job");
    }
}
```

***

## Summary

| Section                     | Details                                                              |
|-----------------------------|----------------------------------------------------------------------|
| Kafka Connectors Setup       | Use `FlinkKafkaConsumer` and `FlinkKafkaProducer` with configs      |
| Maven Dependencies           | Include Flink streaming and Kafka connector jars                     |
| Reading from Kafka           | Create source using consumer, specifying topic and deserializer     |
| Data Transformation          | Use DataStream API operators like `map`                             |
| Writing to Kafka             | Create sink using producer and assign to DataStream sink             |
| Validation                   | Use Kafka UI tools to monitor output topics and messages             |
