Below is a sample `server.properties` file with explanations of its key configurations. This file is used to configure a single Kafka broker.

-----

### Sample `server.properties` File

```properties
############################# Server Basics #############################

# The ID of the broker. Must be unique for each broker in the cluster.
broker.id=0

############################# Listeners #############################

# The address the broker listens on. This is how clients connect.
listeners=PLAINTEXT://:9092

# The address other brokers use to connect to this broker.
# In a single-node setup, this can be the same as 'listeners'.
advertised.listeners=PLAINTEXT://localhost:9092

############################# Log & Data #############################

# The directory where Kafka stores log files for topics and partitions.
log.dirs=/tmp/kafka-logs

# The default number of partitions for newly created topics.
num.partitions=1

# The default replication factor for newly created topics.
# A replication factor of 1 means no data redundancy.
default.replication.factor=1

############################# Log Retention Policy #############################

# The number of hours to keep messages before they are deleted.
log.retention.hours=168

# The maximum size in bytes of a single log segment file.
log.segment.bytes=1073741824

############################# Zookeeper #############################

# The ZooKeeper connection string.
# A comma-separated list of host:port pairs.
zookeeper.connect=localhost:2181

# The timeout in milliseconds for the ZooKeeper session.
zookeeper.connection.timeout.ms=18000
```

-----

### Explanation of Properties

  * **`broker.id`**: Every Kafka broker needs a unique ID. In a multi-broker cluster, each file would have a different ID (e.g., `0`, `1`, `2`).
  * **`listeners` & `advertised.listeners`**: These tell Kafka where to listen for connections. In a simple setup, `listeners` defines the port on which the broker accepts connections, while `advertised.listeners` tells other brokers and clients how to connect to this broker. This is particularly important in Docker or cloud environments where internal and external addresses differ.
  * **`log.dirs`**: This is where all the topic and partition data is saved. You can specify multiple paths, which is a common practice to spread disk I/O across multiple physical drives for better performance.
  * **`num.partitions` & `default.replication.factor`**: These are fallback values for new topics. For new topics, if you don't specify the number of partitions or replication factor when creating them, Kafka will use these values. A **replication factor** of `1` means each partition has only one copy, making it vulnerable to data loss if the broker fails.
  * **`log.retention.hours`**: This is a crucial property for managing disk space. It specifies the duration for which Kafka will keep messages. After this time, old messages are automatically deleted.
  * **`log.segment.bytes`**: This property defines when a new log segment file is created. Once the current file exceeds this size, Kafka rolls over to a new segment. This keeps file sizes manageable and helps with log cleanup.
  * **`zookeeper.connect`**: In older Kafka versions, this property tells the broker which ZooKeeper ensemble to connect to. ZooKeeper manages the state of the Kafka cluster, including leader election for brokers and topics. Note that newer Kafka versions are moving away from ZooKeeper in favor of the KRaft protocol.