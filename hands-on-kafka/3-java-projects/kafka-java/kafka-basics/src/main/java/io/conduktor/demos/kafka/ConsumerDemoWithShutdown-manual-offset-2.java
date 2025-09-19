package io.conduktor.demos.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

// Example consumer storing offsets locally in a file per partition
public class ConsumerWithLocalOffsetStorage {

    private static final Logger log = LoggerFactory.getLogger(ConsumerWithLocalOffsetStorage.class.getSimpleName());

    private static final String GROUP_ID = "local-offset-group";
    private static final String TOPIC = "demo_java";
    private static final String OFFSET_STORE_DIR = "/tmp/kafka_offsets";

    // Thread-safe map to hold current offsets for each partition in memory
    private static final Map<TopicPartition, Long> currentOffsets = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "52.171.63.91:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");  // disable auto commit
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        final Thread mainThread = Thread.currentThread();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detected shutdown, calling consumer.wakeup()");
            consumer.wakeup();
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        // Rebalance Listener to handle partition assignments and revocations
        ConsumerRebalanceListener rebalanceListener = new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                log.info("Partitions revoked: " + partitions);
                // Save offsets for revoked partitions before losing assignment
                commitOffsetsLocally();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                log.info("Partitions assigned: " + partitions);
                // On assignment, seek to the last locally stored offset for each partition, or beginning
                for (TopicPartition partition : partitions) {
                    long offset = readOffsetFromLocalStore(partition);
                    if (offset >= 0) {
                        log.info("Seeking partition " + partition + " to offset " + offset);
                        consumer.seek(partition, offset + 1);
                    } else {
                        log.info("No local offset found for partition " + partition + ", seeking to beginning");
                        consumer.seekToBeginning(Arrays.asList(partition));
                    }
                }
            }
        };

        try {
            // Subscribe with rebalance listener
            consumer.subscribe(Collections.singletonList(TOPIC), rebalanceListener);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Consumed record key: " + record.key() + ", value: " + record.value() +
                            ", partition: " + record.partition() + ", offset: " + record.offset());

                    // Update current offset in memory after processing this record
                    TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                    currentOffsets.put(tp, record.offset());
                }

                // Periodically store offsets to local file after processing records batch
                commitOffsetsLocally();
            }
        } catch (WakeupException e) {
            log.info("Consumer is shutting down");
        } catch (Exception e) {
            log.error("Unexpected error", e);
        } finally {
            try {
                // Save offsets locally before shutdown
                commitOffsetsLocally();
            } catch (Exception ex) {
                log.error("Failed to commit offsets on shutdown", ex);
            }
            consumer.close();
            log.info("Consumer closed");
        }
    }

    // Save offsets from currentOffsets map to local files per partition
    private static void commitOffsetsLocally() {
        for (Map.Entry<TopicPartition, Long> entry : currentOffsets.entrySet()) {
            TopicPartition tp = entry.getKey();
            long offset = entry.getValue();
            writeOffsetToLocalStore(tp, offset);
        }
    }

    // Write offset for a partition to a file in offset store directory
    private static void writeOffsetToLocalStore(TopicPartition tp, long offset) {
        try {
            File dir = new File(OFFSET_STORE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File offsetFile = new File(dir, tp.topic() + "-" + tp.partition() + ".offset");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(offsetFile))) {
                writer.write(Long.toString(offset));
            }
            log.info("Stored offset " + offset + " locally for partition " + tp);
        } catch (IOException e) {
            log.error("Failed to write offset to local store for " + tp, e);
        }
    }

    // Read offset for a partition from file; returns -1 if no offset found
    private static long readOffsetFromLocalStore(TopicPartition tp) {
        File offsetFile = new File(OFFSET_STORE_DIR, tp.topic() + "-" + tp.partition() + ".offset");
        if (!offsetFile.exists()) {
            return -1;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(offsetFile))) {
            String offsetStr = reader.readLine();
            return Long.parseLong(offsetStr);
        } catch (Exception e) {
            log.error("Failed to read offset from local store for " + tp, e);
            return -1;
        }
    }
}
