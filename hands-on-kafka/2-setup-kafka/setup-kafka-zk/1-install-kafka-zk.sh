/usr/local/kafka/bin/kafka-server-stop.sh /usr/local/kafka/config/server.properties
sleep 1
/usr/local/kafka/bin/zookeeper-server-stop.sh /usr/local/kafka/config/zookeeper.properties
sleep 1

sudo apt update -y
sudo apt-get install -y netcat tree zip unzip git

sudo rm -rf /usr/local/kafka/logs/zookeeper.out

sudo apt-get -y install openjdk-11-jdk

wget -nc https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz
tar -zxvf kafka_2.13-3.7.0.tgz
sudo rm -rf /usr/local/kafka
ls -al
sudo mv kafka_2.13-3.7.0 /usr/local/kafka

# Change IP address in below line. Need to put public ip of current machine
echo "advertised.listeners=PLAINTEXT://52.171.63.91:9092" >> /usr/local/kafka/config/server.properties

cat /usr/local/kafka/config/server.properties
nano /usr/local/kafka/config/server.properties

/usr/local/kafka/bin/zookeeper-server-start.sh -daemon /usr/local/kafka/config/zookeeper.properties

sleep 2

nc -vz localhost 2181

/usr/local/kafka/bin/kafka-server-start.sh -daemon /usr/local/kafka/config/server.properties

sleep 2

nc -vz localhost 9092

# Replace with the public ip of currect machine
nc -vz 52.171.63.91 9092

# Change IP address with the public ip of currect machine
cat /usr/local/kafka/logs/kafkaServer.out | grep 52.171.63.91

tail -f /usr/local/kafka/logs/zookeeper.out
# Press CTRL+C to exit

tail -f /usr/local/kafka/logs/kafkaServer.out
# Press CTRL+C to exit

nc -vz localhost 2181
nc -vz localhost 9092

# Change IP address with the public ip of currect machine
nc -vz 52.171.63.91 2181
nc -vz 52.171.63.91 9092

# Change IP address with the public ip of currect machine
/usr/local/kafka/bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server 52.171.63.91:9092

# Change IP address with the public ip of currect machine
/usr/local/kafka/bin/kafka-console-producer.sh --topic quickstart-events --bootstrap-server 52.171.63.91:9092

/usr/local/kafka/bin/kafka-console-consumer.sh --topic quickstart-events --from-beginning --bootstrap-server localhost:9092

#/usr/local/kafka/bin/kafka-server-stop.sh /usr/local/kafka/config/server.properties

#/usr/local/kafka/bin/zookeeper-server-stop.sh /usr/local/kafka/config/zookeeper.properties
