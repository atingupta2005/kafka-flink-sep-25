# Apache Flink: Detailed Practical Guide

***

## 1. What is Apache Flink?

Apache Flink is an open-source unified stream and batch data processing framework designed for high-throughput, low-latency, fault-tolerant distributed computation. Flink's powerful APIs support complex event processing, windowing, and stateful transformations on both bounded (batch) and unbounded (streaming) data sets.

### 1.1 Batch vs Stream Processing

| Batch Processing                 | Stream Processing                       |
|---------------------------------|--------------------------------------|
| Finite data volumes             | Continuous and unbounded data streams |
| Offline, high-latency analytics | Real-time, low latency processing     |
| Data is static and at rest      | Events arrive continuously             |
| Examples: reports, aggregations | Examples: fraud detection, monitoring |

Flink treats batch processing as a special case of streaming by reading bounded data streams, giving a single runtime and programming model.

***

### 1.2 Apache Flink vs Kafka Streams vs Spark Streaming

| Feature               | Apache Flink                              | Kafka Streams                           | Spark Streaming                      |
|-----------------------|-----------------------------------------|----------------------------------------|------------------------------------|
| Processing Model      | Native stream + batch                    | Embeddable library, streaming only    | Micro-batching                     |
| API                   | Java, Scala, Python                      | Java, Scala                           | Java, Scala, Python                |
| State Management      | Distributed snapshots, exactly-once     | Kafka offset + state store             | Checkpointing and logging         |
| Deployment            | Standalone/Cluster/YARN/Kubernetes      | Embedded in consumer apps              | Cluster (Spark)                    |
| Latency               | Milliseconds                            | Milliseconds                         | Seconds                          |
| Scalability           | High (thousands of nodes)                | Medium                              | High                              |
| Use Cases             | Complex event processing, batch streaming | Lightweight stream processing          | Batch+Stream analytics             |

***

## 2. Flink Architecture

***

### 2.1 Components Diagram

```plaintext
+-----------------+          +-----------------+
|     Client      |          |   JobManager    |
| (Submit Job)    +--------->|  (Master node)  |
+-----------------+          +--------+--------+
                                        |
                                        v
                               +------------------+
                               |  TaskManager(s)  |
                               | (Worker nodes)   |
                               +------+-----------+
                                      |
                                      v
           +-----------------+-----------------+-----------------+
           |        Slot          |       Slot         |       Slot     |
           +-----------------+-----------------+-----------------+
           | Tasks of an operator chained per slot                 |
           +-----------------------------------------------------+
```

- **Client** submits a JobGraph to the **JobManager**
- **JobManager** schedules and orchestrates execution, checkpoint coordination
- **TaskManagers** execute parallel subtasks in slots
- Tasks process data streams and maintain state

***

### 2.2 JobManager, TaskManager, Checkpoints

- **JobManager**  
Manages job lifecycle including scheduling, resource allocation, checkpoint coordination.

- **TaskManager**  
Executes tasks; runs operator subtasks with allocated resources and slots.

- **Checkpointing**  
Flink’s fault-tolerance uses distributed snapshots coordinated by JobManager; state saved periodically to durable storage (e.g., HDFS, S3).

***

### 2.3 Parallelism and Scaling

- Flink parallelizes operators into multiple subtasks.
- Parallelism set per job/operator controls task count.
- Slot model: Each TaskManager has fixed number of slots to run tasks.
- Scaling: Add/remove TaskManagers or change parallelism dynamically.

***

### 2.4 Operator Chains and Fault Tolerance

- **Operator Chaining**  
Multiple operators combined in same thread to minimize IPC overhead.

- **Fault Tolerance by Checkpoints**  
Consistent distributed snapshots allow job restart from last successful checkpoint.

***

## 3. Flink Setup on GCP

***

### 3.1 Launch Flink using Docker on GCP

#### Step 1: SSH into GCP VM

```bash
gcloud compute ssh <your-vm-name>
```

***

#### Step 2: Install Docker (if not installed)

```bash
sudo apt update
sudo apt install docker.io
sudo systemctl start docker
sudo systemctl enable docker
```

***

#### Step 3: Run Flink Docker Container

```bash
docker run -d -p 8081:8081 flink:latest
```

- Exposes Flink Web UI at `http://<your-vm-IP>:8081`

***

### 3.2 Submit a Simple Java Job

#### Step 1: Sample Flink WordCount Java Program

```java
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;

public class WordCount {
    public static void main(String[] args) throws Exception {
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        env.readTextFile("hdfs:///input.txt")
           .flatMap((String line, Collector<Tuple2<String, Integer>> out) -> {
               for (String word : line.split(" ")) {
                   out.collect(new Tuple2<>(word, 1));
               }
           })
           .groupBy(0)
           .sum(1)
           .writeAsCsv("hdfs:///output.csv");

        env.execute("WordCount Example");
    }
}
```

***

#### Step 2: Build Job Jar

Use Maven or Gradle to build jar file.

```bash
mvn clean package
```

***

#### Step 3: Submit Job to Flink Cluster

```bash
docker cp target/wordcount.jar <container-id>:/opt/flink/wordcount.jar

docker exec -it <container-id> flink run /opt/flink/wordcount.jar
```

***

### 3.3 Monitor Job in Flink Web UI

- Navigate to `http://<vm-ip>:8081`
- View running jobs, task progress, parallelism, logs
- Access job metrics and checkpoints

***

## Diagrams

### Batch vs Stream Processing Diagram

```plaintext
+=============================+
|         Batch                |
| Fixed data set ------------> | Processed all at once |
|                             | (High latency & throughput) |
+-----------------------------+

+=============================+
|        Stream                |
| Ongoing data events -------> | Process each event as it arrives |
|                             | (Low latency)                  |
+-----------------------------+
```

### JobManager & TaskManager Diagram

![svg](data:image/svg+xml;base64,PHN2ZyBoZWlnaHQ9IjE1MCIgd2lkdGg9IjI1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB4PSIxMCIgeT0iMTAiIHdpZHRoPSIyMjAiIGhlaWdodD0iNTAlIiBzdHJva2U9ImJsYWNrIiBmaWxsPSJsaWdodGdyZXkiLz48dGV4dCB4PSI1NiIgeT0iMzAiIGZvbnQtc2l6ZT0iMTYiIGZpbGw9ImJsYWNrIj5KdWJNYW5hZ2VyPC90ZXh0PjxsaW5lIHgxPSIxMTAiIHkxPSI1MCIgeDI9IjQyNSIgeTI9IjUwIiBzdHJva2U9ImJsYWNrIi8+PHJlY3QgeD0iMTAiIHk9IjYwIiB3aWR0aD0iMjIwIiBoZWlnaHQ9IjUyIiBzdHJva2U9ImJsYWNrIiBmaWxsPSJsaWdodGdyZXkiLz48dGV4dCB4PSIzIiB5PSI4MSIgZm9udC1zaXplPSIxNiIgZmlsbD0iYmxhY2siPlRhc2tNYW5hZ2Vy

