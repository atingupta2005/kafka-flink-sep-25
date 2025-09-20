## Update the plugin.path in kafka connect-standalone properties.
```
cat /usr/local/kafka/config/connect-standalone.properties
echo "plugin.path=/usr/local/kafka/libs" >> /usr/local/kafka/config/connect-standalone.properties
cat /usr/local/kafka/config/connect-standalone.properties
```

```
nano /usr/local/kafka/config/connect-standalone.properties
```

```
key.converter=org.apache.kafka.connect.storage.StringConverter
value.converter=org.apache.kafka.connect.storage.StringConverter
```

```
listeners=http://:8084
```



```
/usr/local/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic my_file_connect_topic_1 --replication-factor 3 --partitions 3
```

## Create a FILES_connect-synk.properties file with below properties in config folder
```
cd
rm FILES_connect-synk.properties
wget --no-check-certificate --no-cache --no-cookies https://raw.githubusercontent.com/atingupta2005/kafka-ey-24/main/hands-on-kafka/5-kafka-connect-files/FILES_connect-synk.properties
cat FILES_connect-synk.properties
#nano FILES_connect-synk.properties
```

```
cp FILES_connect-synk.properties /usr/local/kafka/config/
cat /usr/local/kafka/config/FILES_connect-synk.properties
```

## Start the Kafka Connector:
```
/usr/local/kafka/bin/connect-standalone.sh /usr/local/kafka/config/connect-standalone.properties /usr/local/kafka/config/FILES_connect-synk.properties
```


## Publish data to the topic
- Note: Need to open another terminal to run command below
```
/usr/local/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic my_file_connect_topic_1
```

## Check the text file
- Note: Need to open another terminal to run command below
```
tail -ff  /tmp/my-file-sink.txt
```
