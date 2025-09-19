package io.conduktor.demos.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AvroConsumerExample {

    private static final Logger log = LoggerFactory.getLogger(AvroConsumerExample.class);

    public static void main(String[] args) {
        String groupId = "my-java-application";
        String topic = "demo_java_avro";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "4.245.192.219:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        properties.setProperty("schema.registry.url", "http://4.245.192.219:8081");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // Important: use specific Avro reader to use generated classes
        properties.setProperty("specific.avro.reader", "true");

        KafkaConsumer<String, User> consumer = new KafkaConsumer<>(properties);

        final Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detected a shutdown, calling consumer.wakeup() to interrupt poll");
            consumer.wakeup();
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        try {
            consumer.subscribe(Collections.singletonList(topic));

            while (true) {
                ConsumerRecords<String, User> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, User> record : records) {
                    User user = record.value();
                    log.info("Consumed User - Name: {}, Age: {}, Email: {}",
                            user.getName(), user.getAge(), user.getEmail());
                    log.info("Partition: {}, Offset: {}", record.partition(), record.offset());
                }

                try {
                    consumer.commitSync();
                    log.info("Offsets committed manually");
                } catch (Exception e) {
                    log.error("Failed to commit offsets", e);
                }
            }
        } catch (WakeupException e) {
            log.info("Consumer is shutting down");
        } catch (Exception e) {
            log.error("Unexpected exception in consumer", e);
        } finally {
            consumer.close();
            log.info("Consumer closed gracefully");
        }
    }
}
