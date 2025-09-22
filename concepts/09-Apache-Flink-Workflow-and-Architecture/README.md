# Apache Flink Workflow and Architecture Compared to Apache Spark

***

## 1. Introduction to Flink and Spark

- **Apache Flink** is a distributed stream processing engine optimized for stateful computations on unbounded and bounded data with **true real-time, event-at-a-time processing**.
- **Apache Spark** started as a batch processing engine and later added streaming via **micro-batching** (Structured Streaming). It processes data in small, discrete batch intervals instead of real continuous records.

***

## 2. High-Level Architecture Comparison

| Aspect            | Apache Flink                                 | Apache Spark                              |
|-------------------|---------------------------------------------|------------------------------------------|
| Processing Model   | True streaming (event-by-event)              | Micro-batching (small batches)            |
| Job Submission    | Client creates JobGraph and sends to cluster | Client submits job, Driver manages DAG     |
| Job Orchestration  | Dispatcher receives JobGraph, spawns JobManager | Driver handles everything                  |
| Resource Manager   | ResourceManager allocates slots to TaskManagers | Cluster Manager allocates resources        |
| Task Execution     | TaskManagers run tasks with pipelined execution | Executors run tasks in micro-batch stages |
| Fault Tolerance    | Incremental checkpoints with exactly-once   | Checkpointing between batches               |
| Event Time Support | Advanced watermarking and late event handling | Supported, but less mature                  |
| State Management   | Advanced distributed state backend, RocksDB | Micro-batch based state store               |
| APIs              | DataStream, Table API, SQL                    | RDD, DataFrame, Dataset, SQL                |

***

## 3. Workflow: From Job Submission to Execution

### Flink Workflow

1. **Client Side:**

   - User writes code in Flink API (Java/Scala/Python).
   - Code creates a **StreamGraph** → optimized to a **JobGraph**.
   - JobGraph, along with job JAR/package, is submitted to Flink cluster.

2. **Cluster Side:**

   - **Dispatcher** receives JobGraph, validates and creates a **JobManager** for the job.
   - **JobManager** requests resources from **ResourceManager**, schedules tasks on **TaskManagers**.
   - **TaskManagers** execute operator chains, exchange data via network stack.
   - Periodic **checkpoints** are taken for fault tolerance; on failure, JobManager triggers recovery from checkpoints.
   - Flink cluster monitors and manages backpressure dynamically.

***

### Spark Workflow

1. **Client Side:**

   - User writes Spark code, typically running on a **Driver** node (local or cluster).
   - Spark builds a **logical plan, then physical DAG** internally.
   - Driver manages job submission directly to cluster manager.

2. **Cluster Side:**

   - Cluster manager assigns resources and launches **Executors**.
   - Driver schedules tasks on Executors in **micro-batches**.
   - Data processed in batches; checkpointing happens between batches.
   - Driver manages job fault tolerance, retries tasks if needed.

***

## 4. Architectural Components

### Flink

- **Dispatcher:** Entry point for job submissions; creates JobManagers.
- **JobManager:** Job lifecycle manager; schedules tasks, handles checkpoints, monitors execution.
- **ResourceManager:** Allocates cluster resources as slots to tasks.
- **TaskManager:** Worker nodes that run the actual data processing tasks.
- **State Backend:** RocksDB or memory-based persistent storage for stateful computations.

### Spark

- **Driver:** Central coordinator running on the client or cluster; submits jobs and schedules tasks.
- **Cluster Manager:** External system allocating resources (YARN, Mesos, Kubernetes).
- **Executors:** Workers running on cluster nodes; execute tasks per micro-batch.
- **Storage Layer:** In-memory and disk storage for RDD/DataFrame persistence.

***

## 5. Examples

### Flink Example: Streaming Word Count (Python)

```python
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.common.typeinfo import Types
from pyflink.datastream.functions import FlatMapFunction

class Splitter(FlatMapFunction):
    def flat_map(self, value, collector):
        for word in value.lower().split():
            if word:
                collector.collect((word, 1))

env = StreamExecutionEnvironment.get_execution_environment()
text = env.socket_text_stream("localhost", 9999)

counts = text.flat_map(Splitter(), output_type=Types.TUPLE([Types.STRING(), Types.INT()])) \
             .key_by(lambda x: x[0]) \
             .sum(1)

counts.print()
env.execute("PyFlink Streaming WordCount")
```

### Spark Example: Structured Streaming Word Count (Python)

```python
from pyspark.sql import SparkSession
from pyspark.sql.functions import explode, split, lower, col

spark = SparkSession.builder.appName("SparkStreamingWordCount").getOrCreate()

lines = spark.readStream.format("socket") \
    .option("host", "localhost") \
    .option("port", 9999) \
    .load()

words = lines.select(explode(split(lower(col("value")), "\\W+")).alias("word")) \
             .filter(col("word") != "")

wordCounts = words.groupBy("word").count()

query = wordCounts.writeStream \
    .outputMode("complete") \
    .format("console") \
    .start()

query.awaitTermination()
```

***

## 6. Fault Tolerance

- Flink uses **asynchronous incremental checkpoints** saved to distributed storage (e.g., HDFS), enabling **exactly-once processing**.
- Spark uses **micro-batch checkpoints** at batch boundaries; exactly-once is achieved at batch granularity.

***

## 7. Scaling and Performance

- Flink excels at **low-latency, high-throughput, stateful streaming**.
- Dynamic scaling with resource negotiation and backpressure handling is better in Flink.
- Spark excels in batch processing and is evolving in streaming but with higher latency due to micro-batches.

***
