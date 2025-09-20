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

This is an Avro schema definition in JSON format. It describes the structure of data for a record type named `User`. Here is a breakdown of the schema's components:

* **`namespace`**: `com.example`
    * This is a string that specifies the logical namespace for the schema. It's similar to a Java package or a C++ namespace. When Avro generates code from this schema, this namespace is used to define the package or class location, preventing naming conflicts.

* **`type`**: `record`
    * This is the schema's overall type. The value `record` indicates that this schema defines a complex data structure that is a collection of named fields.

* **`name`**: `User`
    * This is the name of the record type being defined. The full name of the record is the combination of the namespace and the name, which would be `com.example.User`. This name is used to refer to this schema.

* **`fields`**: An array of objects, where each object defines a field within the `User` record. Each field has a `name` and a `type`.

    * **First Field**: `{"name": "name", "type": "string"}`
        * **`name`**: `name`
        * **`type`**: `string`
        * This field is named `name` and it can only contain a string value.

    * **Second Field**: `{"name": "favorite_number", "type": ["int", "null"]}`
        * **`name`**: `favorite_number`
        * **`type`**: `["int", "null"]`
        * This field is named `favorite_number`. The type is an array `["int", "null"]`, which is an Avro-specific syntax for a **union**. This means the field can hold either an integer (`int`) or be `null`. The first type listed in the union is the default or preferred type.

    * **Third Field**: `{"name": "favorite_color", "type": ["string", "null"]}`
        * **`name`**: `favorite_color`
        * **`type`**: `["string", "null"]`
        * This field is named `favorite_color`. Similar to the previous field, its type is a union. It can hold either a string (`string`) or be `null`.

In summary, this schema describes a `User` object that must have a `name` (a string), and optionally can have a `favorite_number` (an integer or null) and a `favorite_color` (a string or null). The use of `null` in a union is the standard Avro way to define optional fields.

### Step 2: Integrate Avro Schema Generation in Gradle

Since you are using a Gradle project in IntelliJ IDEA, the best approach is to use a Gradle plugin to automatically generate Java classes from your Avro schema during the build process.

1.  **Add the Avro Plugin to Your `build.gradle` file**:
    Add the `com.github.davidmc24.gradle.plugin.avro` plugin and the core Avro dependency to your `build.gradle` file. This tells Gradle to handle the schema compilation automatically.

    ```gradle
    plugins {
        id 'java'
        id 'com.github.davidmc24.gradle.plugin.avro' version '1.9.1'
    }

    dependencies {
        implementation 'org.apache.avro:avro:1.11.1'
    }
    ```

2.  **Place Your Avro Schema**:
    Place your Avro schema file (`user.avsc`) in the standard schema directory, which is `src/main/avro`.

3.  **Generate Classes from IntelliJ IDEA**:
    The plugin adds a new task to your Gradle project. To generate the Java classes, open the **Gradle tool window** in IntelliJ IDEA, navigate to `Tasks > avro`, and double-click the **`generateAvroJava`** task. The generated classes will be compiled and made available for use in your project.


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

