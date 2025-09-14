# documentation for the command 
/usr/local/kafka/bin/kafka-consumer-groups.sh 

# list consumer groups
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --list
 
# describe one specific group
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group my-second-application

# describe another group
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group my-first-application

# start a consumer
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic first_topic --group my-first-application

# describe the group now
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group my-first-application

# describe a console consumer group (change the end number)
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group console-consumer-10592

# start a console consumer
/usr/local/kafka/bin/kafka-console-consumer.sh --bootstrap-server $BS_SERVER --topic first_topic --group my-first-application

# describe the group again
/usr/local/kafka/bin/kafka-consumer-groups.sh --bootstrap-server $BS_SERVER --describe --group my-first-application