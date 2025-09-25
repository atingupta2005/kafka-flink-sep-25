# Deploying Apache Kafka on Azure Red Hat OpenShift

---

## 1. Prerequisites

### 1.1 Install `oc` CLI

1. Go to: [https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/](https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/)
2. Download the archive for your OS (Windows, Mac, Linux).
3. Extract it and place the `oc` binary in your PATH.

### 1.2 Connect to OpenShift with `oc`

* From the ARO portal, copy your **API login command**. It looks like:

  ```bash
  oc login <cluster-api-url>
  ```
* After login, check access:

  ```bash
  oc whoami
  oc get nodes
  ```

---

## 2. Install Strimzi Operator (0.45) via Web UI

1. Go to **Operators → OperatorHub**.
2. Search for **Strimzi Kafka Operator**.
3. Choose version **0.45.x**.
4. Install:

   * **Installation mode:** All namespaces or select your project (`kafka`).
   * **Approval strategy:** Automatic.
5. Verify in **Operators → Installed Operators** → Status = ✅ *Succeeded*.

---

## 3. Deploy Kafka Cluster (via Web UI)

1. Go to **Operators → Installed Operators → Strimzi → Kafka**.
2. Click **Create Instance**.
3. Switch to **YAML view**.
4. Paste this configuration:

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: my-cluster
  namespace: kafka
spec:
  kafka:
    replicas: 3
    listeners:
      - name: plain
        port: 9092
        type: internal
        tls: false
      - name: tls
        port: 9093
        type: internal
        tls: true
      - name: external
        port: 9094
        type: loadbalancer   # external access via Azure LoadBalancer
        tls: false
    storage:
      type: ephemeral   # change to persistent for production
  zookeeper:
    replicas: 3
    storage:
      type: ephemeral
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

5. Click **Create**.
6. Check **Workloads → Pods** in `kafka` namespace until all broker and ZooKeeper pods show *Running*.

---

## 4. Access Kafka Services

* Inside cluster: use

  ```
  my-cluster-kafka-bootstrap:9092
  ```
* Outside cluster: check external bootstrap service:

  ```
  oc get svc -n kafka my-cluster-kafka-external-bootstrap
  ```

  → Will show a **public IP** from Azure

---

## 5. Create a Kafka Client Pod (Web UI)

1. Go to **Workloads → Deployments → Create Deployment**.
2. Switch to **YAML view**.
3. Paste this:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka-client
  namespace: kafka
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka-client
  template:
    metadata:
      labels:
        app: kafka-client
    spec:
      containers:
        - name: kafka-client
          image: quay.io/strimzi/kafka:0.47.0-kafka-3.9.0
          command: ["sleep"]
          args: ["infinity"]
```

4. Click **Create**.
5. Once running, connect:

   ```bash
   oc exec -it deploy/kafka-client -n kafka -- bash
   ```

---

## 6. Kafka Operations (Inside Client Pod)

### 6.1 List Topics

```bash
bin/kafka-topics.sh \
  --bootstrap-server my-cluster-kafka-bootstrap:9092 \
  --list
```

### 6.2 Create a Topic

```bash
bin/kafka-topics.sh \
  --bootstrap-server my-cluster-kafka-bootstrap:9092 \
  --create \
  --topic transactions-in \
  --partitions 3 \
  --replication-factor 3
```

### 6.3 Produce Messages

```bash
bin/kafka-console-producer.sh \
  --broker-list my-cluster-kafka-bootstrap:9092 \
  --topic transactions-in
```

👉 Type messages, press Enter. Ctrl+C to exit.

### 6.4 Consume Messages

```bash
bin/kafka-console-consumer.sh \
  --bootstrap-server my-cluster-kafka-bootstrap:9092 \
  --topic transactions-in \
  --from-beginning
```

---

