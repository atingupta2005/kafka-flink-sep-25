package io.conduktor.demos.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getSimpleName());

    public static void main(String[] args) {
        log.info("I am a Kafka Producer!");

        // Create Producer Properties
        Properties properties = new Properties();

        // Specify the Kafka broker(s) address to connect to
        properties.setProperty("bootstrap.servers", "4.245.192.219:9092");

        // Key serializer class to convert keys to byte arrays
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        // Value serializer class to convert values to byte arrays
        properties.setProperty("value.serializer", StringSerializer.class.getName());

        /*
         * Acknowledgements to receive from Kafka broker before considering a request complete:
         * "acks=all" means the leader will wait for the full set of in-sync replicas
         * to acknowledge the record. This ensures the highest level of durability.
         * Use "1" for only leader acknowledgement, or "0" for no acknowledgements.
         * Setting acks to "all" makes it less likely to lose data.
         */
        properties.setProperty("acks", "all");

        /*
         * Number of retries in case of transient failures when sending records.
         * Enables the producer to automatically resend records that fail due to
         * transient broker or network issues.
         * Helps increase message delivery reliability.
         */
        properties.setProperty("retries", "3");

        /*
         * Batch size controls the maximum amount of data to collect before sending
         * messages to the broker. Larger batch sizes can improve throughput by
         * sending more records in fewer requests, but can also increase latency.
         * Setting batch.size to 400 bytes enables small batches to be sent quickly.
         */
        properties.setProperty("batch.size", "400");

        // Create the Kafka producer with the configured properties
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 30; i++) {
                // Create a producer record to send "hello world i" message to topic "demo_java"
                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>("demo_java", "hello world " + i);

                // Send data asynchronously with a callback to get metadata or error info
                producer.send(producerRecord, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if (e == null) {
                            // Successfully produced a record
                            log.info("Received new metadata \n" +
                                    "Topic: " + metadata.topic() + "\n" +
                                    "Partition: " + metadata.partition() + "\n" +
                                    "Offset: " + metadata.offset() + "\n" +
                                    "Timestamp: " + metadata.timestamp());
                        } else {
                            // An error occurred during producing
                            log.error("Error while producing", e);
                        }
                    }
                });
            }

            try {
                // Small pause between batches of messages
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Flush all pending records before closing the producer
        producer.flush();

        // Close the producer cleanly to release resources
        producer.close();
    }
}
