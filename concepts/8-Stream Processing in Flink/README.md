# Java Stream Processing in Apache Flink

***

## 1. Flink DataStream API in Java

Apache Flink’s DataStream API provides a way to build stream processing applications in Java. You operate on streams of data using familiar functional transformations.

### Common Stream Transformations

| Operator | Description | Example                                                          |
|----------|-------------|------------------------------------------------------------------|
| `map`    | Transforms each element individually to another element          | Convert string to uppercase                                        |
| `filter` | Filters elements based on a predicate                             | Pass only numbers greater than 10                                 |
| `keyBy`  | Partitions stream by key for grouped operations                   | Group events by userId                                            |
| `flatMap`| Maps each element to zero or more elements (like a map plus flatten)| Split sentence strings into words                                  |

***

### Code Examples

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

DataStream<String> textStream = env.fromElements("cat", "dog", "elephant");

// map: uppercase each word
DataStream<String> upperStream = textStream.map(String::toUpperCase);

// filter: keep only words longer than 3 chars
DataStream<String> filteredStream = upperStream.filter(word -> word.length() > 3);

// flatMap: split words into characters
DataStream<String> flatMapStream = filteredStream.flatMap((String word, Collector<String> out) -> {
    for (char c : word.toCharArray()) {
        out.collect(String.valueOf(c));
    }
}).returns(Types.STRING);
```

***

## 2. Java Flink Job Example

***

### 2.1 Reading from Socket or File

You can read a stream of data from a socket or a file source.

```java
// Socket example: localhost 9999
DataStream<String> socketStream = env.socketTextStream("localhost", 9999);

// File example: reading lines from a file
DataStream<String> fileStream = env.readTextFile("/path/to/input.txt");
```

***

### 2.2 Applying Transformations and Printing Results

Here’s an example of reading from a socket, transforming data, and printing the output.

```java
DataStream<String> stream = env.socketTextStream("localhost", 9999);

DataStream<String> transformed = stream
    .map(String::toLowerCase)
    .filter(line -> !line.isEmpty());

transformed.print();

env.execute("Socket Stream Processing Example");
```

***

## 3. Windowing & Time Semantics

***

### 3.1 Tumbling and Sliding Windows

- **Tumbling window:** Fixed-size, non-overlapping windows  
- **Sliding window:** Fixed-size windows that overlap by a sliding interval  

```java
dataStream
    .keyBy(value -> value.getKey())
    .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
    .sum("count");
```

```java
dataStream
    .keyBy(value -> value.getKey())
    .window(SlidingProcessingTimeWindows.of(Time.seconds(30), Time.seconds(10)))
    .sum("count");
```

***

### 3.2 Processing Time vs Event Time

| Time Semantics  | Description                                           | Use Case Example                   |
|-----------------|-------------------------------------------------------|----------------------------------|
| Processing Time | Time when event is processed by Flink                 | Simple real-time monitoring       |
| Event Time      | Time when event actually happened, embedded in data   | Accurate results with delayed events |

Set event-time and watermark strategy for event time processing:

```java
env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

dataStream.assignTimestampsAndWatermarks(
  WatermarkStrategy.<MyEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
    .withTimestampAssigner((event, timestamp) -> event.getTimestamp())
);
```

***

### 3.3 Basic Window Aggregation Example

```java
dataStream
    .keyBy(MyEvent::getKey)
    .window(TumblingEventTimeWindows.of(Time.seconds(15)))
    .reduce(new ReduceFunction<MyEvent>() {
        @Override
        public MyEvent reduce(MyEvent value1, MyEvent value2) {
            return new MyEvent(value1.getKey(), value1.getCount() + value2.getCount());
        }
    });
```

***

### 3.4 Checkpointing Basics

- Checkpointing periodically saves state to durable storage (e.g., HDFS or S3)
- Enables fault-tolerant exactly-once processing

Enable checkpointing:

```java
env.enableCheckpointing(10000); // every 10 seconds
```

***

## 4. Debugging and Tuning Flink Jobs

***

### 4.1 Task Parallelism and Thread Utilization

- Default parallelism often equals number of CPU cores  
- Can be set globally or per operator:  
```java
env.setParallelism(4);
dataStream.map(...).setParallelism(2);
```
- Proper parallelism balances load and resource use

***

### 4.2 Backpressure Handling

- Occurs when downstream operators cannot keep up  
- Causes slowdown in upstream operators  
- Detect via Flink UI metrics and logs  
- Fix by tuning parallelism, buffer sizes, or resource allocation

***

### 4.3 Operator Chain Inspection

- Operators may be chained by Flink for optimization  
- Check chaining in Flink Web UI under Job/Task details  
- Break chains with `.disableChaining()` to isolate debugging

***

## Summary

| Topic                      | Key Points                                  |
|----------------------------|---------------------------------------------|
| DataStream API             | Functional operators: map, filter, flatMap, keyBy |
| Reading from Socket/File   | Multiple source options available           |
| Windowing                  | Tumbling/sliding, event time vs processing time |
| Checkpointing              | Automatic fault tolerance                    |
| Debugging and tuning       | Parallelism, backpressure, operator chains  |
