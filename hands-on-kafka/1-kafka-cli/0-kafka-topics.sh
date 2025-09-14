/usr/local/kafka/bin/kafka-topics.sh 

/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --list 

/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic first_topic --create

/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic second_topic --create --partitions 3

/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic third_topic --create --partitions 3 --replication-factor 2

# Create a topic (working)
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic third_topic --create --partitions 3 --replication-factor 1

# List topics
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --list 

# Describe a topic
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic first_topic --describe

# Delete a topic 
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic first_topic --delete
# (only works if delete.topic.enable=true)
