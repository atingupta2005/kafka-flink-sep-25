/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic first_topic --create --partitions 1

# producing
/usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server $BS_SERVER --topic first_topic 
 Hello World
My name is Atin
I love Kafka
^C  (<- Ctrl + C is used to exit the producer)


# producing with properties
/usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server $BS_SERVER --topic first_topic --producer-property acks=all
 some message that is acked
 just for fun


# producing to a non existing topic
/usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server $BS_SERVER --topic new_topic
 hello world!

# our new topic only has 1 partition
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --list
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic new_topic --describe


# edit config/server.properties or config/kraft/server.properties
# num.partitions=3

# produce against a non existing topic again
/usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server $BS_SERVER --topic new_topic_2
hello again!

# this time our topic has 3 partitions
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --list
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server $BS_SERVER --topic new_topic_2 --describe

# overall, please create topics with the appropriate number of partitions before producing to them!


# produce with keys
/usr/local/kafka/bin/kafka-console-producer.sh --bootstrap-server $BS_SERVER --topic first_topic --property parse.key=true --property key.separator=:
name:Atin


