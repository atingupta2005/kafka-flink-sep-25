# Comprehensive Guide: Running Kafka Avro Producer and Consumer with Schema Registry in IntelliJ IDEA


***

## Table of Contents

1. Project Structure and Files
2. Avro Schema Creation and Java Class Generation
3. Setting up Kafka and Schema Registry
4. Running Producer and Consumer Code in IntelliJ IDEA
5. Verifying Schema Registry Usage and Schema Registration
6. Handling Schema Evolution and Schema Drift
7. Troubleshooting
8. Additional Tips and Tools

***

## 1. Project Structure and Files

Your project follows this structure:

```
kafka-basics/
├── src/
│   ├── main/
│   │   ├── avro/
│   │   │   └── io/conduktor/demos/kafka/User.avsc        # Your Avro schema file
│   │   └── java/
│   │       └── io/conduktor/demos/kafka/
│   │           ├── AvroProducerWithGeneratedClass.java   # Producer Java code
│   │           └── AvroConsumerExample.java               # Consumer Java code
├── build/generated/source/avro/main/io/conduktor/demos/kafka/
│   └── User.java                                          # Generated Avro Java class (after build)
├── build.gradle                                           # Gradle build file with Avro plugin & dependencies
└── ...
```


***

## 2. Avro Schema Creation and Java Class Generation

### Creating the Avro Schema

The Avro schema defines the data structure your Kafka messages will follow. Create the file:

`kafka-basics/src/main/avro/io/conduktor/demos/kafka/User.avsc`

with the following content:

```json
{
  "namespace": "io.conduktor.demos.kafka",
  "type": "record",
  "name": "User",
  "fields": [
    { "name": "name", "type": "string" },
    { "name": "age", "type": "int" },
    { "name": "email", "type": ["null", "string"], "default": null }
  ]
}
```


***

### Generating Java Classes from Avro Schema

Use Gradle to generate Java classes from the `.avsc` files:

At the project root (`kafka-basics`), run:

```bash
./gradlew generateAvroJava
```

**What happens:**

- Gradle Avro plugin reads the schema and generates `User.java` class here:
`build/generated/source/avro/main/io/conduktor/demos/kafka/User.java`
- This class includes methods like getters, setters, and builders matching the schema.

Make sure this generated source folder is included in your IDE source paths (usually auto-configured with Gradle import).

***

## 3. Setting up Kafka and Schema Registry

### Start Kafka Broker and Schema Registry

Ensure these services are running and accessible at the host and ports your code expects (`52.171.63.91:9092` for Kafka broker and `http://52.171.63.91:8081` for Schema Registry).

Options:

- **Local development:** Use Confluent Platform quickstart or Docker to run Kafka and Schema Registry locally.
- **Cloud:** Use managed Kafka services (Confluent Cloud, AWS MSK, Azure Event Hubs etc.) with Schema Registry.
- **Verify Topics:** The topic your producer writes to (e.g., `demo_java_avro`) must exist in Kafka.

***

## 4. Running Producer and Consumer in IntelliJ IDEA

### Step 1: Import Project

- Open IntelliJ IDEA
- Import the Gradle project located at `kafka-basics`
- Wait for Gradle sync to complete and dependencies to download


### Step 2: Create Run Configurations

- Go to **Run > Edit Configurations**
- Add **Application** configurations:

**Producer:**
    - Name: `Kafka Avro Producer`
    - Main class: `io.conduktor.demos.kafka.AvroProducerWithGeneratedClass`
    - Module: Your project module

**Consumer:**
    - Name: `Kafka Avro Consumer`
    - Main class: `io.conduktor.demos.kafka.AvroConsumerExample`
    - Module: Your project module


### Step 3: Run Them

- Run the **consumer** config first to start listening on the Kafka topic.
- Run the **producer** config next to send Avro-serialized messages to Kafka.


### Step 4: Observe Logs

- Producer: Logs info about successful sending including topic, partition, offset.
- Consumer: Logs every consumed user message with fields and Kafka metadata.

***

## 5. Verifying Schema Registry Usage and Schema Registration

### How Schema Registry is Used

- Producer registers schemas automatically the first time it sends data with a given schema to a topic.
- Subsequent messages only contain schema IDs, optimizing bandwidth.
- Consumers fetch schemas by ID from Schema Registry as needed for deserialization.


### How to Verify Schema Registration

Use the REST API directly or tools like Postman to check registered schemas:

```bash
curl http://52.171.63.91:8081/subjects/demo_java_avro-value/versions
```

- Returns all schema versions registered for this topic’s value.
- Check for schema compatibility and whether schemas are properly versioned.

***

## 6. Handling Schema Evolution and Schema Drift

### Schema Compatibility Modes

- **Backward:** New schema can read old data.
- **Forward:** Old schema can read new data.
- **Full:** Both backward and forward compatible.
- **None:** No compatibility checks (not recommended).

Configured globally or per subject in Schema Registry.

### Schema Drift and Errors

- Incompatible schemas are rejected at registration time by Schema Registry.
- Producer fails if trying to send with incompatible schema.
- Consumers fail to deserialize if given incompatible or unknown schema versions.
- Handle such failures with retries, logging, or Dead Letter Queues (DLQ).

***

## 7. Troubleshooting Tips

- Check Kafka and Schema Registry connectivity.
- Validate the existence of topics.
- Ensure correct `schema.registry.url` in configs.
- Refresh Gradle project if new dependency or schema changes.
- Check console logs for serialization and schema registration errors.

***

## 8. Additional Tools and Tips

- **IntelliJ Kafka Plugin:**
Install this plugin to browse Kafka topics and view messages in your IDE.
- **Schema Registry UI:**
Use Confluent Control Center or Conduktor UI for schema browsing and management.
- **Test Schema Evolution:**
Use local or test clusters to validate schema changes with compatibility rules before production.

***

