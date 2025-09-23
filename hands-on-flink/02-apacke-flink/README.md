# Real-Time Transaction Processing with Apache Flink and Kafka

## Use Case and Problem Statement

In modern financial systems, massive volumes of transactional data arrive continuously from multiple users and categories. The challenge is to:

- **Process and analyze these transactions in real-time**
- **Aggregate data per user and category within time windows**
- **Handle late-arriving data robustly**
- **Produce downstream summaries continuously for real-time dashboards or auditing**
- **Monitor the health of the streaming job**

This Flink streaming job integrated with Kafka addresses this by building a fault-tolerant, scalable, event-time-aware real-time processing pipeline.

***

## System Architecture and Connectivity

- **Apache Kafka** acts as the distributed, durable **event bus** where transaction messages are published and consumed.
- **Apache Flink** processes incoming Kafka streams, performs windowed aggregations, handles late data, and writes results back to Kafka.
- **Kafka Topics**:
    - `transactions-in`: Raw transaction input data stream
    - `transactions-summary`: Aggregated per-user/category summaries
    - `transactions-late`: Late-arriving events beyond allowed lateness
    - `processing-heartbeat`: Regular status messages based on processing time for monitoring

***

## Data Schema and Structure

Transactions flowing into the system have JSON structure:

```json
{
  "userId": "user123",
  "category": "electronics",
  "amount": 1200.50,
  "timestamp": 1695456000000
}
```

- `userId`: String identifier for the user
- `category`: Category of spending
- `amount`: Transaction amount (double)
- `timestamp`: Event occurrence time in **epoch milliseconds** (event time)

***

## Core Technologies and Concepts

- **Event Time Processing:** Ensures calculations reflect the actual time events occurred, not processing system time.
- **Watermarks:** Progress indicators that enable Flink to manage out-of-order data and when to trigger window computations.
- **Tumbling Windows:** Non-overlapping fixed-size windowing based on event time, e.g., 1-minute windows aggregating transactions.
- **Side Outputs:** Special flows for handling late data arriving after window completion.
- **Exactly-once Semantics with Kafka:** Guarantees every event is processed once despite failures, enabled via Kafka connectors and Flink checkpointing.
- **Processing Time Heartbeats:** System-time-based ticks to monitor job health independently of event data.

***

## Code Walkthrough


***

### 1. StreamExecutionEnvironment Setup

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
```

- The core Flink runtime entry point. All operators and streams are defined within this environment.
- Supports local or cluster execution transparently.

***

### 2. Kafka Configuration

```java
String bootstrapServers = "...";
String inputTopic = "transactions-in";
...
Properties props = new Properties();
props.setProperty("bootstrap.servers", bootstrapServers);
props.setProperty("group.id", groupId);
```

- Kafka bootstrap servers specify cluster addresses for connectivity.
- Topics segregate data streams logically.
- `group.id` ensures correct consumer group coordination on Kafka side.

***

### 3. Kafka Consumer Source

```java
FlinkKafkaConsumer<String> consumer =
    new FlinkKafkaConsumer<>(inputTopic, new SimpleStringSchema(), props);
consumer.setStartFromEarliest();
```

- Defines source for Flink to consume the `transactions-in` topic.
- `SimpleStringSchema` treats each Kafka message as a UTF-8 encoded string.
- Consumes from earliest offset on startup for full data replay during testing.

***

### 4. Kafka Producers Setup

```java
FlinkKafkaProducer<String> summaryProducer = new FlinkKafkaProducer<>(...);
FlinkKafkaProducer<String> lateProducer = new FlinkKafkaProducer<>(...);
FlinkKafkaProducer<String> heartbeatProducer = new FlinkKafkaProducer<>(...);
```

- Producers serialize output strings into bytes and target specific Kafka topics.
- Use `KafkaSerializationSchema` to define serialization and topic assignment.
- Set semantic to `AT_LEAST_ONCE` for robust delivery guarantees.

***

### 5. JSON Parsing with Jackson

```java
DataStream<Transaction> transactions = rawStream
    .map(s -> {
        Transaction tx = mapper.readValue(s, Transaction.class);
        ...
    }).filter(tx -> tx != null && tx.amount > 0);
```

- Maps raw JSON strings into typed `Transaction` objects.
- Logs parsing successes/failures to help diagnose malformed records.
- Filters out invalid or zero-value transactions.

***

### 6. Event Time and Watermarking

```java
transactions = transactions.assignTimestampsAndWatermarks(
    WatermarkStrategy
       .<Transaction>forBoundedOutOfOrderness(java.time.Duration.ofSeconds(10))
       .withTimestampAssigner((tx, ts) -> tx.timestamp)
       .withIdleness(java.time.Duration.ofMinutes(1))
);
```

- Extracts timestamps from event data itself (`tx.timestamp`).
- Defines allowed 10 seconds of out-of-orderness.

***

### 7. Late Event Handling (Side Outputs)

```java
final OutputTag<Transaction> lateTag = new OutputTag<Transaction>("late-transactions") {};
```

- Designates side output stream for events arriving later than allowed lateness (10 seconds after window close).

***

### 8. Key Partitioning and Windowing

```java
SingleOutputStreamOperator<String> aggregated = transactions
    .keyBy(tx -> tx.userId + "|" + tx.category)
    .window(TumblingEventTimeWindows.of(Time.minutes(1)))
    .allowedLateness(Time.seconds(10))
    .sideOutputLateData(lateTag)
    .process(new TransactionAggregatorProcess());
```

- Keys stream by composite key of user and category for isolated aggregation.
- Uses 1-minute tumbling windows defining discrete intervals on event time.
- Enables late data inclusion within 10 seconds.
- Uses the custom process function to produce aggregated JSON strings.

***

### 9. Aggregation with ProcessWindowFunction

- `TransactionAggregatorProcess` iterates windowed transactions, calculates sum, count, average, and other metadata.
- Converts the aggregation result to JSON for downstream consumption.

***

### 10. Data Sink and Heartbeat

```java
aggregated.print().name("Print Aggregated Output");
aggregated.addSink(summaryProducer);
```

- Debug output: prints aggregated summaries on console.
- Sink: writes aggregation results back to Kafka topic `transactions-summary`.

Late events serialized and sent to `transactions-late`.

Heartbeat stream emits constant ticks based on **processing time** scanning system clock, for monitoring via `processing-heartbeat` Kafka topic.

***

### 11. Job Execution

```java
env.execute("Enhanced Kafka Transaction Pipeline (Event + Processing Time)");
```

- Starts the streaming job within Flink cluster or local environment.

***

## Best Practices and Extensions

- **Start with `setStartFromEarliest()`** when testing to consume all historic data.
- Use **watermarks plus idleness** to control timely window firing.
- Handle **late data** with side outputs for completeness.
- Combine **event time for correctness** and **processing time for operational monitoring**.
- Ensure Kafka topics exist with appropriate partitions before job start.
- Make use of Flink’s **checkpoint and restart mechanisms** to guarantee fault tolerance.
- Extend with real-time alerts or enrichment stages based on business needs.

***

## Summary

This pipeline exemplifies how Flink and Kafka complement each other for **fault-tolerant, scalable, real-time analytics**. The architecture enables:

- Robust ingestion from Kafka,
- Correctly ordered event processing using event time,
- Windowed aggregation with stateful computation,
- Correct handling of late-arriving data,
- Integration of runtime monitoring, and
- Delivery guarantees through Kafka producers.

This detailed explanation and annotated code prepares students for practical real-world streaming data applications using modern big-data tools.

