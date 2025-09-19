# 1. Overview of ksqlDB

ksqlDB is a streaming SQL engine built on Apache Kafka that allows you to process and analyze real-time data streams using familiar SQL syntax. It works directly on Kafka topics and supports continuous queries, defining streams and tables over your data.

***

## 2. Launching ksqlDB Server Using Docker

### Why Use Docker?

- Docker allows quick and isolated setup of ksqlDB services without manual installs.
- Simplifies dependencies like Kafka broker, Zookeeper, Schema Registry, and ksqlDB server.

### Step-by-Step Docker Setup

#### Step 1: Install Docker

- Download and install Docker Desktop from the official Docker website for your OS.
- Ensure Docker is running and allocate sufficient memory (at least 8 GB for Kafka stacks).

#### Step 2: Create a Docker Compose File

Create a file named `docker-compose.yml` with the following minimal stack to run Kafka, Zookeeper, Schema Registry, ksqlDB server, and ksqlDB CLI:

```yaml
version: '3.7'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  schema-registry:
    image: confluentinc/cp-schema-registry:latest
    depends_on:
      - kafka
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: PLAINTEXT://kafka:9092

  ksqldb-server:
    image: confluentinc/ksqldb-server:latest
    depends_on:
      - kafka
      - schema-registry
    ports:
      - "8088:8088"
    environment:
      KSQL_CONFIG_DIR: "/etc/ksqldb"
      KSQL_BOOTSTRAP_SERVERS: kafka:9092
      KSQL_HOST_NAME: ksqldb-server
      KSQL_LISTENERS: http://0.0.0.0:8088
      KSQL_SCHEMA_REGISTRY_URL: http://schema-registry:8081

  ksqldb-cli:
    image: confluentinc/ksqldb-cli:latest
    depends_on:
      - ksqldb-server
    entrypoint: /bin/sh
    tty: true
```

#### Step 3: Start ksqlDB Stack

Run the following in the terminal inside the directory where `docker-compose.yml` is:

```bash
docker-compose up -d
```

This command starts all services in detached mode. You can check services status with:

```bash
docker-compose ps
```

***

### Step 4: Connect to the ksqlDB CLI

Run the CLI container interactively:

```bash
docker exec -it <container_id_or_name_of_ksqldb-cli> ksql http://ksqldb-server:8088
```

You can find the container name using:

```bash
docker ps
```

Now you are connected to the ksqlDB CLI prompt, ready to execute SQL statements.

***

## 3. Register Kafka Topic as STREAM

### What is a STREAM in ksqlDB?

A STREAM represents a continuous flow of real-time data from a Kafka topic. You create streams over topics to query and process streaming data.

### Creating a STREAM

Assuming you have a Kafka topic named `user_events` handling JSON-encoded user activity records with fields `userid` and `eventtype`, you create a ksqlDB stream like this:

```sql
CREATE STREAM user_events_stream (
    userid VARCHAR,
    eventtype VARCHAR
) WITH (
    KAFKA_TOPIC = 'user_events',
    VALUE_FORMAT = 'JSON'
);
```

The `WITH` clause instructs ksqlDB to connect the stream to the correct Kafka topic and interpret data format.

***

## 4. Writing Simple Queries in ksqlDB

Once your STREAM is created, you can start running continuous queries on it.

***

### SELECT

Query all data from the stream:

```sql
SELECT * FROM user_events_stream EMIT CHANGES;
```

This prints every event that flows into the stream in real-time.

***

### FILTER

Filter records matching some criteria, for example, events where `eventtype` is `'login'`:

```sql
SELECT * FROM user_events_stream WHERE eventtype = 'login' EMIT CHANGES;
```

***

### WHERE

WHERE clause is part of filtering in SELECT, e.g., filtering by multiple conditions:

```sql
SELECT userid, eventtype 
FROM user_events_stream 
WHERE userid = 'user123' AND eventtype = 'purchase' 
EMIT CHANGES;
```

***

### Notes on Queries

- `EMIT CHANGES` is required in ksqlDB 0.10+ for continuous SELECT statements on streams.
- Queries will keep running and display data matching the criteria as new records arrive.

***

## 5. Useful Commands and Tips

### Listing STREAMS

To list all streams in your ksqlDB environment:

```sql
SHOW STREAMS;
```

### Describe a STREAM

Get metadata about the stream's schema:

```sql
DESCRIBE user_events_stream;
```

### Terminate Long Running Queries

To terminate a query run previously, use:

```sql
TERMINATE <query_id>;
```

You can obtain query IDs using:

```sql
SHOW QUERIES;
```

***

## Summary

- Docker is an efficient way to run and manage ksqlDB server and dependencies.
- You create STREAMS in ksqlDB to map Kafka topics for querying.
- Use SQL queries such as SELECT, FILTER, WHERE to continuously process and analyze live data.
- The ksqlDB CLI provides an interactive environment to run SQL statements and explore streams.
- Understanding these fundamentals is essential for building real-time streaming applications with ksqlDB.

***
