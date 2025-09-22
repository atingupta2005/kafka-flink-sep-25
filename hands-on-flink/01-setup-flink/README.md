# Apache Flink Local Setup Using Docker Compose

This guide explains how to set up an Apache Flink cluster locally using Docker Compose. It uses the official Apache Flink Docker image, deploys a JobManager and two TaskManagers, and exposes the Flink Web UI for monitoring.

***

## Prerequisites

- Docker installed and running on your system  
- Docker Compose (supports version 3.8 syntax)

***

## Docker Compose File

The following `docker-compose.yml` defines your Flink cluster:

```yaml
version: "3.8"

services:
  flink-jobmanager:
    image: apache/flink:1.19.3
    container_name: flink-jobmanager
    ports:
      - "8085:8081"       # Flink Web UI on host port 8085
      - "6123:6123"       # JobManager RPC port
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
      - FLINK_ENV_JAVA_OPTS=-Xms1g -Xmx2g
    command: jobmanager
    networks:
      - flink-net
    volumes:
      - ./config:/opt/flink/config

  flink-taskmanager-1:
    image: apache/flink:1.19.3
    container_name: flink-taskmanager-1
    depends_on:
      - flink-jobmanager
    ports:
      - "6122:6122"       # TaskManager RPC port 1
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
      - TASK_MANAGER_NUMBER_OF_TASK_SLOTS=2
      - FLINK_ENV_JAVA_OPTS=-Xms1g -Xmx2g
    command: taskmanager
    networks:
      - flink-net
    volumes:
      - ./config:/opt/flink/config

  flink-taskmanager-2:
    image: apache/flink:1.19.3
    container_name: flink-taskmanager-2
    depends_on:
      - flink-jobmanager
    ports:
      - "6124:6124"       # TaskManager RPC port 2
    environment:
      - JOB_MANAGER_RPC_ADDRESS=flink-jobmanager
      - TASK_MANAGER_NUMBER_OF_TASK_SLOTS=2
      - FLINK_ENV_JAVA_OPTS=-Xms1g -Xmx2g
    command: taskmanager
    networks:
      - flink-net
    volumes:
      - ./config:/opt/flink/config

networks:
  flink-net:
```

***

## Steps to Start the Flink Cluster

1. **Create configuration folder** (optional but recommended)  
   Create a `config` folder in the same directory to hold Flink configuration files such as `flink-conf.yaml` for tuning.

2. **Start the cluster**  
   Run the following command in the directory containing your Docker Compose file:

   ```bash
   docker compose up -d
   ```

3. **Verify the cluster status**  
   - Access Flink Web UI at [http://localhost:8085](http://localhost:8085)  
   - The dashboard shows running JobManager and TaskManagers.

4. **Submitting Flink jobs**  
   - You can submit your Flink jobs (Java, Scala, Python) via CLI or web UI.  
   - Example CLI submit command inside the JobManager container:

     ```bash
     docker exec -it flink-jobmanager /bin/bash
     flink run /path/to/your-job.jar
     ```

***

## Notes

- The Flink Web UI is exposed on port **8085** to avoid conflicting with other services (e.g., Kafka Schema Registry uses 8081).
- TaskManager slots are set to 2 each, modify `TASK_MANAGER_NUMBER_OF_TASK_SLOTS` to control parallelism.
- Adjust JVM memory settings in `FLINK_ENV_JAVA_OPTS` as needed.
- You can scale the number of TaskManagers horizontally by copying `flink-taskmanager` service blocks or using `docker compose up --scale`.
