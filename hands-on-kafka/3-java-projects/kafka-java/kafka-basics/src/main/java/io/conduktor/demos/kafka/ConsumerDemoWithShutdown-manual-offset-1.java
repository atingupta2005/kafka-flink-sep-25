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

public class ConsumerDemoWithShutdownmanualoffset1. {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class.getSimpleName());

    public static void main(String[] args) {
        log.info("I am a Kafka Consumer!");

        String groupId = "my-java-application";
        String topic = "demo_java";

        Properties properties = new Properties();

        // Kafka broker address
        properties.setProperty("bootstrap.servers", "4.245.192.219:9092");
        // Deserializer classes for key/value
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("group.id", groupId);

        // Start reading from the earliest message when no prior offset is committed
        properties.setProperty("auto.offset.reset", "earliest");

        // Disable auto commit for manual offset control
        properties.setProperty("enable.auto.commit", "false");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        final Thread mainThread = Thread.currentThread();

        // Graceful shutdown hook to interrupt polling
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Detected a shutdown, calling consumer.wakeup() to interrupt poll");
                consumer.wakeup();
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            consumer.subscribe(Arrays.asList(topic));

            while (true) {
                // Poll for new records
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                // Process each record in the batch
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                }

                // Manually commit offsets synchronously after processing the batch.
                // Ensures no offset is committed before records are processed.
                try {
                    consumer.commitSync();
                    log.info("Offsets have been committed manually");
                } catch (Exception e) {
                    log.error("Failed to commit offsets", e);
                }
            }
        } catch (WakeupException e) {
            log.info("Consumer is starting to shut down");
        } catch (Exception e) {
            log.error("Unexpected exception in the consumer", e);
        } finally {
            // Close consumer – commits offsets and cleans up resources
            consumer.close();
            log.info("The consumer is now gracefully shut down");
        }
    }
}
