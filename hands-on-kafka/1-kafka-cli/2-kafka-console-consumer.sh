# create a topic with 3 partitions
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic second_topic --create --partitions 3

# consuming
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic second_topic

# other terminal
/usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server $BS_SERVER --producer-property partitioner.class=org.apache.kafka.clients.producer.RoundRobinPartitioner --topic second_topic

# consuming from beginning
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic second_topic --from-beginning

# display key, values and timestamp in consumer
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic second_topic --formatter kafka.tools.DefaultMessageFormatter --property print.timestamp=true --property print.key=true --property print.value=true --property print.partition=true --from-beginning