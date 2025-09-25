package com.example.flink;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.errors.TopicExistsException;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class KafkaTopicManagerAndProducer {

    //private static final String BOOTSTRAP_SERVERS = "4.245.192.219:9092,4.245.192.219:9093,4.245.192.219:9094";
    private static final String BOOTSTRAP_SERVERS = "20.213.222.81:9094";

    public static void main(String[] args) throws Exception {
        String inputTopic = "transactions-in";

        // Create topic if not exists
        createTopicIfNotExists(inputTopic, 3, (short) 1);

        // Start producing data continuously to input topic
        produceTestData(inputTopic);
    }

    // Create Kafka topic if not already exists
    public static void createTopicIfNotExists(String topicName, int numPartitions, short replicationFactor) throws Exception {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(props)) {
            // Check existing topics
            Set<String> topics = adminClient.listTopics().names().get();

            if (topics.contains(topicName)) {
                System.out.println("Topic already exists: " + topicName);
                return;
            }

            // Create new topic
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
            CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));

            try {
                result.all().get();
                System.out.println("Topic created: " + topicName);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof TopicExistsException) {
                    System.out.println("Topic already exists (race condition): " + topicName);
                } else {
                    throw e;
                }
            }
        }
    }

    // Produce sample transaction JSON messages continuously to the Kafka input topic
    public static void produceTestData(String topicName) throws InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            Random rnd = new Random();
            String[] users = {"u1", "u2", "u3"};
            String[] categories = {"food", "electronics", "transport"};

            while (true) {
                String user = users[rnd.nextInt(users.length)];
                String category = categories[rnd.nextInt(categories.length)];
                double amount = rnd.nextDouble() * 1000; // Random amount 0-1000
                long timestamp = System.currentTimeMillis();

                String json = String.format(
                        "{\"userId\":\"%s\",\"category\":\"%s\",\"amount\":%.2f,\"timestamp\":%d}",
                        user, category, amount, timestamp);

                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, null, json);
                producer.send(record);

                System.out.println("Produced: " + json);

                Thread.sleep(1000); // Produce 1 message per second
            }
        }
    }
}
