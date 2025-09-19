# Introduction to Schema Registry

## What is a Schema Registry?

A **Schema Registry** is a centralized service that manages and stores schemas for data being produced and consumed in distributed systems like Kafka. It acts as a schema repository that producers and consumers interact with to ensure the data's structure (schema) is consistent and evolves safely over time.

***

## Why Use a Schema Registry?

1. **Enforce Data Contracts:**
With a Schema Registry, producers and consumers agree on the structure of the data by sharing schemas. This enforces a strong data contract between different components of a distributed system and prevents runtime serialization/deserialization errors.
2. **Schema Evolution and Compatibility:**
The Schema Registry allows schemas to evolve safely over time. It supports compatibility checks between schema versions (backward, forward, full compatibility), ensuring that new versions of schemas do not break existing consumers or producers.
3. **Reduce Errors and Data Corruption:**
Without a centralized schema, producers and consumers may use different assumptions about the data structure, which can cause hard-to-debug errors. The Schema Registry prevents this by validating all data against the registered schemas.
4. **Decouple Producer and Consumer Development:**
Producers can evolve their schema as long as it adheres to compatibility rules. Consumers can read older or newer versions safely by referencing the schema stored centrally.

***

## Role in Enforcing Data Contracts

- When producers send data, they serialize it according to a schema stored in the Schema Registry.
- The Schema Registry assigns a unique schema ID to the schema and attaches this ID to the serialized message.
- Consumers retrieve the schema using the schema ID and deserialize the data accordingly.
- This process enforces that both producers and consumers agree on the schema structure.
- Compatibility checking in Schema Registry ensures that schema changes do not break consumers.

***

## Supported Formats: Avro

- **Avro** is the most commonly used serialization format with Schema Registries in Kafka.
- Avro supports a compact binary format with a schema that describes the data structure.
- The schema is stored externally in the Schema Registry rather than sent with every message, which saves bandwidth.
- Avro schemas are written in JSON and describe record fields and data types.
- Other serialization formats (Protobuf, JSON Schema) can also be supported by some Schema Registries but Avro remains popular in Kafka ecosystems.

***

## Example: Using Schema Registry with Avro in Java

### Step 1: Define an Avro Schema (Example: `user.avsc`)

```json
{
  "namespace": "com.example",
  "type": "record",
  "name": "User",
  "fields": [
    {"name": "name", "type": "string"},
    {"name": "favorite_number", "type": ["int", "null"]},
    {"name": "favorite_color", "type": ["string", "null"]}
  ]
}
```


### Step 2: Generate Java Classes from Avro Schema

Using the Avro Maven plugin or `avro-tools` CLI, generate Java classes from the `.avsc` file:

```bash
java -jar avro-tools-1.10.2.jar compile schema user.avsc ./src/main/java
```


### Step 3: Configure Producer and Consumer to use Schema Registry

Add necessary dependencies in your Maven `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>io.confluent</groupId>
    <artifactId>kafka-avro-serializer</artifactId>
    <version>7.4.0</version>
  </dependency>
  <dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.10.2</version>
  </dependency>
</dependencies>
```


### Step 4: Java Producer Example

```java
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import java.util.Properties;

public class AvroProducer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", KafkaAvroSerializer.class.getName());
        props.put("value.serializer", KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        Producer<String, User> producer = new KafkaProducer<>(props);

        User user = User.newBuilder()
                .setName("Alice")
                .setFavoriteNumber(7)
                .setFavoriteColor("blue")
                .build();

        ProducerRecord<String, User> record = new ProducerRecord<>("users", "user1", user);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                exception.printStackTrace();
            } else {
                System.out.println("Record sent to partition " + metadata.partition() + " with offset " + metadata.offset());
            }
        });

        producer.flush();
        producer.close();
    }
}
```


### Step 5: Java Consumer Example

```java
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AvroConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "avro-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", "true");

        KafkaConsumer<String, User> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("users"));

        while (true) {
            ConsumerRecords<String, User> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, User> record : records) {
                User user = record.value();
                System.out.printf("Consumed user: %s, favorite number: %d, favorite color: %s%n",
                        user.getName(), user.getFavoriteNumber(), user.getFavoriteColor());
            }
        }
    }
}
```

