# Introduction to ksqlDB


***

## 1. What is ksqlDB?

ksqlDB is a powerful event streaming database purpose-built for Apache Kafka. It provides a lightweight, distributed SQL engine designed specifically for stream processing. With ksqlDB, users can query, transform, and process real-time data streams using familiar SQL syntax without the complexity of writing Java or Scala code.

***

## 2. SQL for Streams

### Why SQL for Streams?

Traditional databases use SQL for querying static data stored in tables. ksqlDB extends this SQL to **streams of data**, enabling real-time analytics and transformations over continuously flowing data.

### How ksqlDB Uses SQL

- **CREATE STREAM** and **CREATE TABLE** statements allow you to define streams and tables over Kafka topics.
- Continuous **SELECT** statements operate on data streams.
- SQL constructs such as joins, filters, aggregations, and windowing operate on streams in a similar way to relational databases, but with streaming semantics.

***

## 3. Key Concepts in ksqlDB

### STREAM

- A **STREAM** in ksqlDB represents an unbounded, append-only sequence of data records (events).
- Each event is an immutable fact, never changing once produced.
- Events arrive continuously and are stored in Kafka topics.
- Streams model **event logs** where each new event adds information.
- You can think of a stream like a log of all changes or activities.


### TABLE

- A **TABLE** in ksqlDB models a **stateful view** or the **current state** of a dataset.
- It represents the latest value for each key. It is **updatable** or **mutable**.
- Internally, a TABLE is built from a changelog stream—a stream of events that modify the current state.
- Tables offer a point-in-time snapshot rather than a history of changes.
- Think of a table as a database table reflecting current inventory levels, user profiles, or account balances.

***

## 4. Difference Between STREAM and TABLE in Detail

| Aspect | STREAM | TABLE |
| :-- | :-- | :-- |
| Data Model | Unbounded sequence of immutable events | Stateful representation of current state |
| Storage | Append-only Kafka topic | Derived from changelog stream based on keys |
| Semantics | Event log where each event is stored | Updates overwrite previous state per key |
| Key Requirement | Optional | Required to identify state updates |
| Use Case Examples | User activity logs, order history | Inventory snapshots, account balances |
| Query Type | Real-time event processing, alerting | Point-in-time lookups, current state joins |
| Growth Behavior | Continually grows with new events | Grows only when new keys are added |
| Replaying History | Supports replaying entire history | Provides snapshot view; history lost except via changelog |


***

### Visual Example Illustration

Imagine two users moving between locations, producing movement events:


| Event Number | User | Location |
| :-- | :-- | :-- |
| 1 | Robin | Ilkley |
| 2 | Allison | Boulder |
| 3 | Robin | London |

- As a **STREAM**, these events would be listed sequentially: all movements, full history.
- As a **TABLE**, the state would be updated and last known location is stored: Robin → London, Allison → Boulder.

***

## 5. Why Are Both Useful?

- **Streams** capture full immutable event histories, ideal for auditing, replaying, or event sourcing.
- **Tables** provide quick access to the current state, needed for queries that require latest values.
- Applications combine both: e.g., "orders" as streams (all order events) and "inventory" as tables (current stock).
- ksqlDB enables you to write queries combining streams and tables for powerful real-time analytics.

***

## 6. Simple ksqlDB Statements Examples

### Creating a Stream

```sql
CREATE STREAM user_location_stream (
    username VARCHAR,
    location VARCHAR
) WITH (
    kafka_topic='user_locations',
    value_format='JSON'
);
```


### Creating a Table

```sql
CREATE TABLE user_latest_location (
    username VARCHAR PRIMARY KEY,
    location VARCHAR
) WITH (
    kafka_topic='user_locations_changelog',
    key='username',
    value_format='JSON'
);
```


***

