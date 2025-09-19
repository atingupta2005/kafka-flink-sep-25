# Avro Serialization in Java

## Introduction to Avro Serialization

Apache Avro is a compact, fast, binary serialization framework primarily used with Big Data systems like Apache Kafka. Avro uses **schemas** written in JSON to define data structure, enabling interoperability and schema evolution.

Serialization means converting Java objects into compact binary data according to an Avro schema for efficient data transfer or storage. Deserialization is reconstructing Java objects from the binary data.

***

## Generating Avro Schemas

### Avro Schema Definition

An Avro schema is a JSON file defining the data structure:

```json
{
  "namespace": "io.conduktor.demos.kafka",
  "type": "record",
  "name": "User",
  "fields": [
    {"name": "name", "type": "string"},
    {"name": "age", "type": "int"},
    {"name": "email", "type": ["null", "string"], "default": null}
  ]
}
```

- This schema defines a record `User` with `name` (string), `age` (int), and optional `email`.
- Place the schema in `src/main/avro/io/conduktor/demos/kafka/User.avsc` for Gradle-based projects with Avro plugin.


### Compile Schema to Java Classes

Use Avro Gradle plugin or Avro tools CLI to automatically generate Java classes from `.avsc` files:

- Gradle:

```
./gradlew generateAvroJava
```

- CLI:

```
java -jar avro-tools-1.11.1.jar compile schema src/main/avro/io/conduktor/demos/kafka/User.avsc src/main/java
```


Generated classes include getters, setters, builders, and schema metadata.

***

## Produce Avro Data

### Configure Producer

- Use `KafkaAvroSerializer` as the value serializer.
- Provide Schema Registry URL (`schema.registry.url`).
- Serialize objects using generated Avro classes or GenericRecord.


### Producer Code Example

```java
Properties props = new Properties();
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
props.put("schema.registry.url", "http://localhost:8081");

KafkaProducer<String, User> producer = new KafkaProducer<>(props);

User user = User.newBuilder()
                .setName("Alice")
                .setAge(30)
                .setEmail("alice@example.com")
                .build();

ProducerRecord<String, User> record = new ProducerRecord<>("users-avro", "user1", user);

producer.send(record, (metadata, exception) -> {
    if (exception == null) {
        System.out.println("Produced record to topic " + metadata.topic());
    } else {
        exception.printStackTrace();
    }
});

producer.flush();
producer.close();
```


***

## Consume Avro Data

### Configure Consumer

- Use `KafkaAvroDeserializer` as the value deserializer.
- Set `specific.avro.reader` to `true` to consume as generated classes.
- Provide Schema Registry URL.


### Consumer Code Example

```java
Properties props = new Properties();
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
props.put("schema.registry.url", "http://localhost:8081");
props.put(ConsumerConfig.GROUP_ID_CONFIG, "avro-consumer-group");
props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
props.put("specific.avro.reader", "true");

KafkaConsumer<String, User> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Collections.singletonList("users-avro"));

while (true) {
    ConsumerRecords<String, User> records = consumer.poll(Duration.ofMillis(1000));
    for (ConsumerRecord<String, User> record : records) {
        User user = record.value();
        System.out.printf("Consumed user: %s, age: %d, email: %s%n",
                user.getName(), user.getAge(), user.getEmail());
    }
}
```


***

## Summary

| Topic | Details |
| :-- | :-- |
| Avro Schema | JSON-based schema defines the structure and types of records |
| Schema Compilation | Avro plugin or CLI generates Java classes used in serialization |
| Serialization | Convert Java Avro objects to compact binary data for transmission or storage |
| Deserialization | Convert binary data back to Java Avro objects using schema |
| Kafka Integration | Use `KafkaAvroSerializer` \& `KafkaAvroDeserializer` with Schema Registry URL configured |

Avro serialization supports schema evolution, compact data encoding, and enforces strong data contracts between producers and consumers.
