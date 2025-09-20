# Kafka Clients – Debugging & Tuning

Kafka clients (producers and consumers) need careful **debugging, monitoring, and tuning** for high reliability and performance. This document explains each configuration in depth, shows their purpose, impact, and trade-offs, and provides **Java code examples**.

---

## 1. Logs and Metrics

### 1.1 Overview

Kafka provides **logging** and **metrics** to help you understand client behavior, detect errors, and optimize performance.

* **Logging**: Shows internal client operations like retries, connections, metadata refresh, and errors.
* **Metrics**: Exposed via **JMX** and programmatically, they quantify throughput, latency, error rates, consumer lag, and resource utilization.

**Why it matters:**

* Helps identify **network issues**, **broker unavailability**, or **slow consumers**.
* Enables proactive tuning of batch sizes, timeouts, and retries.

**Example log4j2.yaml for Kafka clients:**

```yaml
status: WARN
appenders:
  console:
    name: Console
    type: Console
    layout:
      type: PatternLayout
      pattern: "[%d{HH:mm:ss}] %-5p %c{1}:%L - %m%n"
loggers:
  kafkaRoot:
    level: INFO
    AppenderRef:
      - ref: Console
  kafkaProducer:
    level: DEBUG  # Enable detailed producer logs (retries, batching)
    AppenderRef:
      - ref: Console
  kafkaConsumer:
    level: DEBUG  # Enable detailed consumer logs (polls, fetches)
    AppenderRef:
      - ref: Console
```

### 1.2 Important Kafka Metrics

| Metric Category                | Key Metrics                                                                           | Explanation & Use                                                                                                                                                                       |
| ------------------------------ | ------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Producer Metrics**           | `record-send-rate`, `record-error-rate`, `request-latency-avg`, `compression-rate`    | `record-send-rate`: messages/sec, helps gauge throughput; `record-error-rate`: shows errors; `request-latency-avg`: network/ack latency; `compression-rate`: efficiency of compression. |
| **Consumer Metrics**           | `records-consumed-rate`, `fetch-latency-avg`, `commit-latency-avg`, `records-lag-max` | Shows consumption rate, how fast messages are fetched, offset commit delays, and lag per partition.                                                                                     |
| **Network/Connection Metrics** | `io-ratio`, `connection-count`, `request-rate`                                        | Tracks network usage, open connections, and request rates to detect bottlenecks.                                                                                                        |

**Java Example – Printing Metrics**

```java
KafkaProducer<String, String> producer = new KafkaProducer<>(props);
producer.send(new ProducerRecord<>("my-topic", "key", "value"));

producer.metrics().forEach((name, metric) -> {
    System.out.println(name.name() + " -> " + metric.metricValue());
});
```

> Metrics give **quantitative insight** into throughput, latency, retries, and batching performance.

---

## 2. Producer – Retries and Timeouts

### 2.1 Key Configurations

| Config                | Detailed Explanation                                                                                                                   | Trade-offs / Best Practices                                                                         |
| --------------------- | -------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------- |
| `enable.idempotence`  | Ensures **no duplicate messages** per partition during retries.                                                                        | Must be `true` for exactly-once semantics. Slightly increases internal overhead.                    |
| `acks`                | Minimum number of broker acknowledgments before considering a message sent. `0` = no ack, `1` = leader ack, `all` = leader + replicas. | `all` ensures durability, `1` is faster but less safe.                                              |
| `retries`             | Number of retry attempts on transient failures.                                                                                        | Combined with idempotence prevents duplicates. Higher retries = more durability but longer latency. |
| `retry.backoff.ms`    | Time to wait between retries.                                                                                                          | Helps avoid overwhelming the broker. Default is 100ms; adjust for network stability.                |
| `delivery.timeout.ms` | Maximum time for a message to be delivered. Includes retries and backoff.                                                              | Messages exceeding this timeout are marked failed. Must be >= `linger.ms + request.timeout.ms`.     |

**Java Example:**

```java
props.put("enable.idempotence", "true");
props.put("acks", "all");
props.put("retries", 5);
props.put("retry.backoff.ms", 200);
props.put("delivery.timeout.ms", 30000);
```

**Concept:**
Retries handle **temporary failures** (network, leader change). Idempotence ensures **no duplicates** even if retries occur.

---

### 2.2 Throughput Tuning for Producer

| Config                                  | Explanation                                                  | Impact / Trade-off                                                                                |
| --------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------- |
| `linger.ms`                             | Maximum time to wait before sending a batch.                 | Larger linger allows **larger batches** → higher throughput, slightly higher latency.             |
| `batch.size`                            | Max bytes per batch per partition.                           | Larger batch = fewer requests → higher throughput. Too large may delay small messages.            |
| `compression.type`                      | Compress messages (`none`, `gzip`, `snappy`, `lz4`, `zstd`). | Reduces network usage, increases CPU usage. `snappy` is good balance.                             |
| `max.in.flight.requests.per.connection` | Max unacknowledged requests per connection.                  | Must be <= 5 for idempotent producer to maintain order. Higher = faster but may reorder messages. |

**Java Example:**

```java
props.put("linger.ms", 20);         // Wait 20ms for batch
props.put("batch.size", 32 * 1024); // 32 KB batch size
props.put("compression.type", "snappy");
props.put("max.in.flight.requests.per.connection", 5);
```

**Concept:**
Batching + compression improves throughput. Adjust **linger and batch size** to balance latency vs throughput.

---

## 3. Consumer – Throughput and Timeouts

| Config                      | Detailed Explanation                                                                        | Trade-offs / Best Practices                                                                 |
| --------------------------- | ------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- |
| `max.poll.records`          | Max messages returned per poll.                                                             | Higher = better throughput, but consumer takes longer to process each poll.                 |
| `fetch.min.bytes`           | Minimum bytes fetched per request.                                                          | Larger = fewer network calls, may increase latency.                                         |
| `fetch.max.wait.ms`         | Max time to wait to fill `fetch.min.bytes`.                                                 | Balances latency vs batch size.                                                             |
| `max.partition.fetch.bytes` | Max bytes fetched per partition.                                                            | Must accommodate large messages; too high → memory pressure.                                |
| `session.timeout.ms`        | Time to detect dead consumers.                                                              | Too low → frequent rebalances; too high → slow failure detection.                           |
| `isolation.level`           | `read_committed` ensures consumer sees **only committed transactions** (important for EOS). | Default is `read_uncommitted`. Use `read_committed` when consuming transactional producers. |

**Java Example:**

```java
consumerProps.put("max.poll.records", 500);
consumerProps.put("fetch.min.bytes", 16 * 1024);
consumerProps.put("fetch.max.wait.ms", 200);
consumerProps.put("max.partition.fetch.bytes", 1024 * 1024);
consumerProps.put("session.timeout.ms", 15000);
consumerProps.put("isolation.level", "read_committed");
```

**Concept:**
Consumer throughput is influenced by **poll size, fetch size, and session timeout**. Proper tuning avoids **lag, rebalances, or overloading consumer memory**.

---

## 4. Java Example – Producer and Consumer Together

```java
// Producer
KafkaProducer<String, String> producer = new KafkaProducer<>(props);
for (int i = 0; i < 10; i++) {
    ProducerRecord<String, String> record = new ProducerRecord<>("my-topic", "key-" + i, "value-" + i);
    producer.send(record).get(); // synchronous send for demonstration
}
producer.metrics().forEach((name, metric) -> System.out.println(name.name() + " -> " + metric.metricValue()));
producer.close();

// Consumer
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
consumer.subscribe(Collections.singletonList("my-topic"));
while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        System.out.printf("Consumed: key=%s, value=%s, partition=%d, offset=%d%n",
                record.key(), record.value(), record.partition(), record.offset());
    }
}
```

**Key Takeaways:**

* Producer handles **retries, batching, compression, and idempotence**.
* Consumer handles **poll size, fetch configuration, session timeout, and isolation level**.
* **Metrics + logging** provide insight to tune both for optimal performance.

---

## 5. Conceptual Summary Table

| Component | Key Config                              | Purpose                   | Notes / Trade-offs                              |
| --------- | --------------------------------------- | ------------------------- | ----------------------------------------------- |
| Producer  | `enable.idempotence`                    | Prevent duplicates        | Required for EOS                                |
| Producer  | `acks`                                  | Message durability        | `all` for reliability, `1` for speed            |
| Producer  | `retries`                               | Handle transient failures | Combine with idempotence                        |
| Producer  | `linger.ms`                             | Batch delay               | Higher = more throughput, higher latency        |
| Producer  | `batch.size`                            | Batch size per partition  | Larger batch = higher throughput                |
| Producer  | `compression.type`                      | Network efficiency        | CPU overhead vs network savings                 |
| Consumer  | `max.poll.records`                      | Poll batch size           | Large = better throughput but slower processing |
| Consumer  | `fetch.min.bytes` / `fetch.max.wait.ms` | Fetch tuning              | Balance latency and throughput                  |
| Consumer  | `max.partition.fetch.bytes`             | Partition fetch limit     | Must accommodate large messages                 |
| Consumer  | `session.timeout.ms`                    | Detect dead consumers     | Too low = frequent rebalances                   |
| Consumer  | `isolation.level`                       | Committed messages        | Use `read_committed` for transactional producer |

---
