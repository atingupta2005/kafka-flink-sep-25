# create a topic with 3 partitions
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic third_topic --create --partitions 3

# start one consumer
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic third_topic --group my-first-application

# start one producer and start producing
/usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server $BS_SERVER --producer-property partitioner.class=org.apache.kafka.clients.producer.RoundRobinPartitioner --topic third_topic

# start another consumer part of the same group. See messages being spread
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic third_topic --group my-first-application

# start another consumer part of a different group from beginning
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic third_topic --group my-second-application --from-beginning