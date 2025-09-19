# Hands-On Guide: Running ksqlDB and Basic Queries

***

## Prerequisites

- Docker installed and running (allocate at least 8GB RAM)
- Basic familiarity with Kafka and command-line
- Optional: Docker Compose installed

***

## Step 1: Launch ksqlDB Server Using Docker

### 1.1 Create `docker-compose.yml`

Create a file named `docker-compose.yml` with below contents:

```yaml
version: '3.7'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.1
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.1
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  schema-registry:
    image: confluentinc/cp-schema-registry:7.4.1
    depends_on:
      - kafka
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: PLAINTEXT://kafka:9092

  ksqldb-server:
    image: confluentinc/ksqldb-server:7.4.2
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
    image: confluentinc/ksqldb-cli:7.4.2
    depends_on:
      - ksqldb-server
    entrypoint: /bin/sh
    tty: true
```

***

### 1.2 Start Docker Stack

In terminal at the directory containing `docker-compose.yml`, run:

```bash
docker-compose up -d
```

This downloads images and starts services: Zookeeper, Kafka, Schema Registry, ksqlDB Server & CLI.

***

### 1.3 Confirm Services Are Running

Check with:

```bash
docker-compose ps
```

Look for all services up, especially `ksqldb-server` on port 8088.

***

### 1.4 Connect to ksqlDB CLI

Run:

```bash
docker exec -it <ksqldb-cli-container-name> ksql http://ksqldb-server:8088
```

You can find container name via:

```bash
docker ps
```

Expected prompt:

```
ksql>
```

You are now connected to ksqlDB CLI.

***

## Step 2: Register Kafka Topic as STREAM

Assuming you have a Kafka topic named **user_events** already existing.

### 2.1 Create a ksqlDB Stream

At the `ksql>` prompt, run:

```sql
CREATE STREAM user_events_stream (
    userid VARCHAR,
    eventtype VARCHAR,
    eventtime BIGINT
) WITH (
    KAFKA_TOPIC = 'user_events',
    VALUE_FORMAT = 'JSON'
);
```

This creates a ksqlDB stream tied to the Kafka topic `user_events`, interpreting incoming data as JSON.

***

### 2.2 Verify Stream Creation

List streams:

```sql
SHOW STREAMS;
```

Describe the stream schema:

```sql
DESCRIBE user_events_stream;
```

***

## Step 3: Write Simple Queries (SELECT, FILTER, WHERE)

### 3.1 Query all records (press Ctrl+C to stop)

```sql
SELECT * FROM user_events_stream EMIT CHANGES;
```

This starts streaming all records arriving on the stream.

***

### 3.2 Filtering with WHERE clause

Filter only login events:

```sql
SELECT * FROM user_events_stream WHERE eventtype='login' EMIT CHANGES;
```

This will continuously output only login events.

***

### 3.3 Selecting specific columns

```sql
SELECT userid, eventtype FROM user_events_stream EMIT CHANGES;
```

***

### 3.4 Stop Queries

Type:

```sql
TERMINATE <query_id>;
```

Get running queries and IDs with:

```sql
SHOW QUERIES;
```

***

## Additional Tips

- Use `PRINT 'topic_name' FROM BEGINNING;` to view raw Kafka topic data.
- Use `DESCRIBE EXTENDED stream_name;` for detailed stream info.
- For Avro data, use `VALUE_FORMAT='AVRO'` in the stream creation.
- Use ksqlDB REST API for programmatic query execution.

***

## Cleanup

Stop all services with:

```bash
docker-compose down
```

***
