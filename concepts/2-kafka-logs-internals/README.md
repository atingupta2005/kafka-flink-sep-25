### How to Access and Review Kafka Log Files

To access and review Kafka log files, you first need to get an interactive shell inside the Kafka container. From there, you navigate to the data directory, and then use specific commands to inspect the raw message logs.

---

### Accessing the Kafka Container

You use the **`docker exec`** command to run a command inside a running container.

**`docker exec -it kafka1 bash`**

* **`docker exec`**: Executes a command in a running container.
* **`-it`**: The interactive and pseudo-terminal flags. These are essential for getting a usable shell.
* **`kafka1`**: The name of your Kafka container.
* **`bash`**: The command to start a BASH shell inside the container.

This command gives you a prompt like `[appuser@kafka1 ~]$`, indicating you're now inside the container.

---

### Navigating to the Log Directory

Once inside the container, Kafka stores its messages in a specific directory. You can use standard Linux commands to navigate to it.

**`cd /var/lib/kafka/data`**

* **`cd`**: Changes the directory.
* **`/var/lib/kafka/data`**: The default path for Kafka's log data. This directory contains subdirectories for each topic partition.

**`ls -l`**

This command lists the contents of the data directory. You'll see directories named in the format **`topic_name-partition_number`**, such as `atin_topic-0`, `second_topic_aj-2`, and internal topics like `__consumer_offsets-0`.

---

### Understanding the Files in a Partition

When you enter a partition's directory, for example, `cd atin_topic-0`, you will find several key files. A Kafka log is composed of a sequence of these file segments.

* **`*.log`**: The **log segment file**. This is a binary file that stores the actual Kafka messages in an append-only format. The filename, like `00000000000000000000.log`, indicates the **base offset**, which is the starting offset of the first message in this segment.
* **`*.index`**: The **offset index file**. This file is a sparse index that maps message offsets to their physical positions (byte offsets) within the corresponding `.log` file. This allows Kafka to quickly locate messages without scanning the entire log.
* **`*.timeindex`**: The **timestamp index file**. This index maps message timestamps to their offsets. It is used to quickly find messages based on when they were produced.
* **`partition.metadata`**: A file containing critical metadata for the partition, such as the ID of the current leader broker.

---

### Reviewing Messages with `DumpLogSegments`

To read the raw, binary contents of a `.log` file, you use the `kafka.tools.DumpLogSegments` utility.

**`/usr/bin/kafka-run-class kafka.tools.DumpLogSegments --files /var/lib/kafka/data/atin_topic-0/00000000000000000000.log --print-data-log`**

* **`/usr/bin/kafka-run-class`**: A script that runs any Kafka tool by setting up the correct Java environment.
* **`kafka.tools.DumpLogSegments`**: The specific tool for reading and dumping the contents of a log segment file.
* **`--files [path_to_log_file]`**: Specifies the `.log` file you want to inspect.
* **`--print-data-log`**: The flag that tells the tool to print the message payloads, not just the metadata about them.

The output will show a detailed breakdown of each message, including its offset, timestamp, key, and the data payload, which in your case is JSON. This confirms that messages are being written correctly to the disk.