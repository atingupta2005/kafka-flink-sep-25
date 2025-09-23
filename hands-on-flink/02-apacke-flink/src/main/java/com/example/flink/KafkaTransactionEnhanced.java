package com.example.flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.*;
import org.apache.flink.util.OutputTag;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Properties;

public class KafkaTransactionEnhanced {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka configuration inline
        String bootstrapServers = "4.245.192.219:9092,4.245.192.219:9093,4.245.192.219:9094";
        String inputTopic = "transactions-in";
        String summaryTopic = "transactions-summary";
        String lateEventsTopic = "transactions-late";
        String heartbeatTopic = "processing-heartbeat";
        String groupId = "flink-transaction-enhanced";

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", bootstrapServers);
        props.setProperty("group.id", groupId);

        ObjectMapper mapper = new ObjectMapper();

        // Kafka consumer
        FlinkKafkaConsumer<String> consumer =
                new FlinkKafkaConsumer<>(inputTopic, new SimpleStringSchema(), props);
        consumer.setStartFromEarliest();


        // Kafka producer for aggregated summaries
        FlinkKafkaProducer<String> summaryProducer = new FlinkKafkaProducer<>(
                bootstrapServers,
                new KafkaSerializationSchema<String>() {
                    @Override
                    public ProducerRecord<byte[], byte[]> serialize(String element, @Nullable Long timestamp) {
                        return new ProducerRecord<>(summaryTopic, element.getBytes());
                    }
                },
                props,
                FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);

        // Kafka producer for late events
        FlinkKafkaProducer<String> lateProducer = new FlinkKafkaProducer<>(
                bootstrapServers,
                new KafkaSerializationSchema<String>() {
                    @Override
                    public ProducerRecord<byte[], byte[]> serialize(String element, @Nullable Long timestamp) {
                        return new ProducerRecord<>(lateEventsTopic, element.getBytes());
                    }
                },
                props,
                FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);

        // Kafka producer for heartbeat messages
        FlinkKafkaProducer<String> heartbeatProducer = new FlinkKafkaProducer<>(
                bootstrapServers,
                new KafkaSerializationSchema<String>() {
                    @Override
                    public ProducerRecord<byte[], byte[]> serialize(String element, @Nullable Long timestamp) {
                        return new ProducerRecord<>(heartbeatTopic, element.getBytes());
                    }
                },
                props,
                FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);

        // Source stream from Kafka
        DataStream<String> rawStream = env.addSource(consumer);

        DataStream<Transaction> transactions = rawStream
                .map(s -> {
                    try {
                        Transaction tx = mapper.readValue(s, Transaction.class);
                        System.out.println("Parsed event timestamp: " + tx.timestamp);
                        return tx;
                    } catch (Exception e) {
                        System.err.println("Failed to parse incoming JSON: " + s);
                        return null;
                    }
                })
                .filter(tx -> tx != null && tx.amount > 0);

        // Assign timestamps and watermarks based on event time (transaction.timestamp)
        transactions = transactions.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<Transaction>forBoundedOutOfOrderness(java.time.Duration.ofSeconds(10))
                        .withTimestampAssigner((tx, ts) -> tx.timestamp)
                        .withIdleness(java.time.Duration.ofMinutes(1))
        );


        // Side output tag for late events
        final OutputTag<Transaction> lateTag = new OutputTag<Transaction>("late-transactions") {
        };

        // Aggregate per (userId + category) in tumbling event-time windows with allowed lateness
        SingleOutputStreamOperator<String> aggregated = transactions
                .keyBy(tx -> tx.userId + "|" + tx.category)
                .window(TumblingEventTimeWindows.of(Time.minutes(1)))
                .allowedLateness(Time.seconds(10))
                .sideOutputLateData(lateTag)
                .process(new TransactionAggregatorProcess());

        // Sink: write aggregated summary JSON to Kafka
        aggregated.print().name("Print Aggregated Output");
        aggregated.addSink(summaryProducer);

        // Sink for late events (serialize back to JSON) to Kafka late events topic
        DataStream<Transaction> lateEvents = aggregated.getSideOutput(lateTag);
        lateEvents
                .map(tx -> {
                    try {
                        return mapper.writeValueAsString(tx);
                    } catch (Exception e) {
                        return "{\"error\":\"could not serialize late event\"}";
                    }
                })
                .addSink(lateProducer);

        // Emit periodic processing time heartbeat messages every second
        env.fromSequence(1, Long.MAX_VALUE)
                .map(i -> "ProcessingTime Heartbeat @ " + new Date())
                .returns(String.class)
                .setParallelism(1)
                .addSink(heartbeatProducer)
                .setParallelism(1);

        env.execute("Enhanced Kafka Transaction Pipeline (Event + Processing Time)");
    }
}
