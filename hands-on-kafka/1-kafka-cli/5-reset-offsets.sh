# look at the documentation again
/usr/local/kafka/bin/kafka-consumer-groups.sh

# describe the consumer group
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group my-first-application

# Dry Run: reset the offsets to the beginning of each partition
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --group my-first-application --reset-offsets --to-earliest --topic third_topic --dry-run

# execute flag is needed
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --group my-first-application --reset-offsets --to-earliest --topic third_topic --execute

# describe the consumer group again
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group my-first-application

# consume from where the offsets have been reset
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic third_topic --group my-first-application

# describe the group again
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group my-first-application