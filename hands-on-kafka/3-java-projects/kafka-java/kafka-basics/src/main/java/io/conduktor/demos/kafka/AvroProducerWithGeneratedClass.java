package io.conduktor.demos.kafka;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class AvroProducerWithGeneratedClass {

    private static final Logger log = LoggerFactory.getLogger(AvroProducerWithGeneratedClass.class);

    public static void main(String[] args) {
        String topic = "demo_java_avro";

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "4.245.192.219:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://4.245.192.219:8081");

        KafkaProducer<String, User> producer = new KafkaProducer<>(props);

        // Create User object (generated Avro class)
        User user = User.newBuilder()
                .setName("Atin")
                .setAge(48)
                .setEmail("atin@atin.com")
                .build();

        ProducerRecord<String, User> record = new ProducerRecord<>(topic, "user1", user);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Error sending record", exception);
            } else {
                log.info("Record sent to topic {} partition {} offset {}", metadata.topic(), metadata.partition(), metadata.offset());
            }
        });

        producer.flush();
        producer.close();
    }
}
