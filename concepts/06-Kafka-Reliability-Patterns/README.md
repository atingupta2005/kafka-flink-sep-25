# Kafka Reliability Patterns

---

## 1. Idempotent Producer

```java
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

public class IdempotentProducerExample {
    public static void main(String[] args) throws Exception {
        // Step 1: Configure Kafka producer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092"); // Kafka broker(s)
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Enable idempotence to avoid duplicate messages during retries
        props.put("enable.idempotence", "true");
        props.put("acks", "all"); // Ensure leader + replicas acknowledge message
        props.put("retries", Integer.MAX_VALUE); // Retry indefinitely on transient failures
        props.put("max.in.flight.requests.per.connection", "5"); // Safe ordering

        // Step 2: Create Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Step 3: Send messages in a loop
        for (int i = 0; i < 10; i++) {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>("my-topic", "key-" + i, "value-" + i);

            // Step 4: Send message asynchronously
            // send() returns a Future<RecordMetadata> which can be used to check success/failure
            Future<RecordMetadata> future = producer.send(record);

            // Optional: block and get metadata (synchronous behavior)
            RecordMetadata metadata = future.get(); 
            System.out.println("Sent message: " + record.value() + 
                               " to partition " + metadata.partition() + 
                               " with offset " + metadata.offset());
        }

        // Step 5: Close producer
        producer.close();
    }
}
```

**Explanation of `Future`:**

* The `send()` method in Kafka is **asynchronous**.
* It immediately returns a `Future<RecordMetadata>` which can be used to:

  * Wait for the message to be acknowledged (`future.get()`)
  * Check for exceptions or failures
* Using `Future` is optional; if you don’t call `get()`, Kafka will send messages asynchronously for better performance.

---

## 2. Exactly-Once Semantics (Transactional Producer)

```java
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class TransactionalProducerExample {
    public static void main(String[] args) {
        // Step 1: Configure Kafka producer for transactions
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        props.put("enable.idempotence", "true"); // Prevent duplicate messages
        props.put("acks", "all");                // Ensure full replication
        props.put("retries", Integer.MAX_VALUE); // Retry indefinitely
        props.put("transactional.id", "my-transactional-producer"); // Unique per producer instance

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // Step 2: Initialize transactions
        producer.initTransactions();

        try {
            // Step 3: Begin a transaction
            producer.beginTransaction();

            // Step 4: Send multiple messages atomically
            producer.send(new ProducerRecord<>("my-topic", "key1", "value1"));
            producer.send(new ProducerRecord<>("my-topic", "key2", "value2"));

            // Step 5: Commit transaction
            producer.commitTransaction(); // All messages are now atomically visible
            System.out.println("Transaction committed successfully");
        } catch (Exception e) {
            // Step 6: Abort transaction in case of failure
            producer.abortTransaction(); // Messages sent in this transaction are discarded
            System.out.println("Transaction aborted due to error: " + e.getMessage());
        } finally {
            // Step 7: Close producer
            producer.close();
        }
    }
}
```

**Key Notes:**

* `transactional.id` is **unique per producer instance**; it allows the broker to track ongoing transactions.
* `initTransactions()` must be called before sending transactional messages.
* `beginTransaction()` / `commitTransaction()` / `abortTransaction()` control **atomicity**.

---

## 3. Consumer with `read_committed` Isolation Level

```java
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ReadCommittedConsumerExample {
    public static void main(String[] args) {
        // Step 1: Configure consumer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "my-consumer-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // Step 2: Only read committed messages (important for EOS)
        props.put("isolation.level", "read_committed");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("my-topic"));

        // Step 3: Poll and process messages
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("Consumed record: key=%s, value=%s, partition=%d, offset=%d%n",
                        record.key(), record.value(), record.partition(), record.offset());

                // Here you can commit offsets manually if needed (transactionally for EOS)
            }
        }
    }
}
```

**Explanation of `read_committed`:**

* Ensures consumers **only see messages from committed transactions**.
* Messages from aborted transactions are **ignored**.
* Required for **Exactly-Once Semantics** to prevent duplicate or partial processing.

---

## 4. Summary of Key Concepts

| Concept                          | Explanation                                                                                              |
| -------------------------------- | -------------------------------------------------------------------------------------------------------- |
| **Idempotent Producer**          | Guarantees no duplicate messages per partition during retries.                                           |
| **Future<RecordMetadata>**       | Returned by `send()`; can be used to **synchronously check** if the message was delivered successfully.  |
| **Transactional Producer**       | Combines idempotence with transactions to achieve **atomic writes** across multiple messages/partitions. |
| **Exactly-Once Semantics (EOS)** | Ensures **end-to-end single processing**: producer → Kafka → consumer → offset commit.                   |
| **`read_committed`**             | Consumer isolation level to **only see committed transactions**, required for EOS.                       |

