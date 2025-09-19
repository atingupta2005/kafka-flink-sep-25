package io.conduktor.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoWithShutdown {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName());

    public static void main(String[] args) {
        log.info("I am a Kafka Consumer!");

        // Kafka consumer group id.
        // Consumers with the same group id share the work of consuming records
        // from the subscribed topics, allowing load balancing.
        String groupId = "my-java-application";

        // Topic to subscribe to
        String topic = "demo_java";

        // Consumer configuration properties
        Properties properties = new Properties();

        // Kafka broker address to connect to
        properties.setProperty("bootstrap.servers", "4.245.192.219:9092");

        // Deserializer classes to convert byte arrays to String for key and value
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());

        /*
         * Group id used to identify the consumer group this consumer belongs to.
         * Kafka ensures each partition is consumed by only one consumer in the group.
         */
        properties.setProperty("group.id", groupId);

        /*
         * Controls what to do when there is no committed offset for this consumer's group
         * or if the committed offset is invalid (e.g., if logs were truncated).
         * "earliest": consumer starts reading from the beginning of the partition log
         * "latest": consumer starts from the end (new messages only)
         * Setting to "earliest" ensures consumer gets all historical records.
         */
        properties.setProperty("auto.offset.reset", "earliest");

        // Create the Kafka consumer with above properties
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Reference to the main thread to allow graceful shutdown
        final Thread mainThread = Thread.currentThread();

        // Adding a shutdown hook to handle SIGTERM or other termination signals gracefully
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
                // This will interrupt consumer.poll() and throw a WakeupException
                consumer.wakeup();
                try {
                    // Wait for main thread to finish processing before shutting down
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            // Subscribe consumer to the topic(s)
            consumer.subscribe(Arrays.asList(topic));

            // Polling loop - Kafka consumer uses poll() to fetch records continuously
            while (true) {
                // Poll for new records with a timeout of 1000ms
                // poll() drives all I/O including heartbeat and partition rebalances
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                // Process each record received in this poll batch
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }

                /*
                 * Offset committing:
                 * In this example, offsets are committed automatically on consumer.close().
                 * Alternatively, you could call consumer.commitSync() or consumer.commitAsync()
                 * here to commit offsets after processing records to avoid data loss/duplication.
                 * Committing offsets means Kafka keeps track of the last processed record offset so
                 * consumers can resume from there in case of failures.
                 */
            }
        } catch (WakeupException e) {
            // Expected exception during shutdown to exit polling loop
            log.info("Consumer is starting to shut down");
        } catch (Exception e) {
            log.error("Unexpected exception in the consumer", e);
        } finally {
            // Close the consumer to commit offsets and cleanup resources
            consumer.close();
            log.info("The consumer is now gracefully shut down");
        }
    }
}
