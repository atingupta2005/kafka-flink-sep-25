# Install Kafka 4.0 on OpenShift using Streams for Apache Kafka

This guide shows how to install **Kafka 4.0 (KRaft mode)** on OpenShift using **Streams for Apache Kafka** operator. All steps use the **OpenShift Web Console**.

---

## Install the Operator

1. Log in to OpenShift Web Console.
2. Go to **Operators → OperatorHub**.
3. Search for **Streams for Apache Kafka** (Red Hat).
4. Click the operator and then **Install**.
5. On the install page:

   * **Installation Mode**: All namespaces on the cluster
   * **Installed Namespace**: `openshift-operators`
   * **Channel**: stable (or 3.0 if listed)
   * **Approval**: Automatic
6. Click **Subscribe**.
7. Go to **Operators → Installed Operators**, filter by `openshift-operators`. Wait until status is **Succeeded**.

---

## Create a Project

1. Go to **Home → Projects → Create Project**.
2. Enter name: `kafka`.
3. Click **Create**.
4. Switch to the `kafka` project in the Console.

---

## Deploy the Kafka Cluster

1. Go to **Operators → Installed Operators → Streams for Apache Kafka**.
2. Under **Kafka** resource, click **Create Kafka**.
3. Switch to **YAML view** and paste:

```yaml
kind: Kafka
apiVersion: kafka.strimzi.io/v1beta2
metadata:
  name: my-cluster
  annotations:
    strimzi.io/node-pools: enabled
    strimzi.io/kraft: enabled
spec:
  kafka:
    config:
      offsets.topic.replication.factor: 3
      transaction.state.log.replication.factor: 3
      transaction.state.log.min.isr: 2
      default.replication.factor: 3
      min.insync.replicas: 2
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
        type: loadbalancer
        tls: false
    version: "4.0.0"
    metadataVersion: "4.0"
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

4. Click **Create**.

---

## Create NodePools

### Broker NodePool

1. In the operator page, choose **KafkaNodePool → Create Instance**.
2. Paste:

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
  name: broker
  labels:
    strimzi.io/cluster: my-cluster
spec:
  replicas: 3
  roles:
    - broker
  storage:
    type: jbod
    volumes:
      - id: 0
        type: persistent-claim
        size: 100Gi
        class: managed-csi
```

3. Click **Create**.

### Controller NodePool

1. Again, create another **KafkaNodePool**.
2. Paste:

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
  name: controller
  labels:
    strimzi.io/cluster: my-cluster
spec:
  replicas: 3
  roles:
    - controller
  storage:
    type: jbod
    volumes:
      - id: 0
        type: persistent-claim
        size: 20Gi
        class: managed-csi
        kraftMetadata: shared
```

3. Click **Create**.

---

# Verify the Kafka Cluster

1. Go to **Workloads → Pods** in the `kafka` project.

   * You should see:

     * 3 controller pods (names include `controller`)
     * 3 broker pods (names include `broker`)
     * 1 entity-operator pod
   * All pods should be **Running**.

2. Go to **Storage → PersistentVolumeClaims**.

   * You should see 3 PVCs for brokers (100Gi) and 3 PVCs for controllers (20Gi).
   * All PVCs should be **Bound**.

3. Go to **Networking → Services**.

   * Check for `my-cluster-kafka-bootstrap` (internal access).
   * Check for the external service (type: LoadBalancer) on port 9094.

---
