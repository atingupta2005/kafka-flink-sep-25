# Secure Kafka 4.0 on OpenShift with SCRAM

## 1. Install the Kafka Operator

1. Log in to the **OpenShift Web Console**.
2. Go to **Operators → OperatorHub**.
3. Search for **Streams for Apache Kafka (Red Hat)**.
4. Click the operator → **Install**.
5. On the install page:

   * **Installation Mode**: All namespaces on the cluster
   * **Installed Namespace**: `openshift-operators`
   * **Channel**: `stable` (or `3.0` if available)
   * **Approval**: Automatic
6. Click **Subscribe**.
7. Verify under **Operators → Installed Operators** → namespace `openshift-operators`. Status must be **Succeeded**.

---

## 2. Create a Project (Namespace)

1. Go to **Home → Projects → Create Project**.
2. Name it: `kafka`.
3. Click **Create**.
4. Switch to the `kafka` project.

---

## 3. Deploy the Kafka Cluster

1. In the Operator page → **Kafka → Create Kafka**.
2. Switch to **YAML view** and paste:

📄 **kafka.yaml**

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: kafka
  namespace: kafka
  annotations:
    strimzi.io/node-pools: enabled
    strimzi.io/kraft: enabled
spec:
  kafka:
    version: "4.0.0"
    metadataVersion: "4.0"
    authorization:
      type: simple
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
      - name: extscram
        port: 9094
        type: loadbalancer
        tls: true
        authentication:
          type: scram-sha-512
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

3. Click **Create**.

---

## 4. Create NodePools

### Broker NodePool

📄 **broker-pool.yaml**

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
  name: broker
  namespace: kafka
  labels:
    strimzi.io/cluster: kafka
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

---

### Controller NodePool

📄 **controller-pool.yaml**

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaNodePool
metadata:
  name: controller
  namespace: kafka
  labels:
    strimzi.io/cluster: kafka
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

---

## 5. Create Topics

📄 **orders-topic.yaml**

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: orders-topic
  namespace: kafka
  labels:
    strimzi.io/cluster: kafka
spec:
  partitions: 3
  replicas: 3
```

📄 **payments-topic.yaml**

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaTopic
metadata:
  name: payments-topic
  namespace: kafka
  labels:
    strimzi.io/cluster: kafka
spec:
  partitions: 3
  replicas: 3
```

---

## 6. Create Users with SCRAM

📄 **orders-user.yaml**

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaUser
metadata:
  name: orders-app-user
  namespace: kafka
  labels:
    strimzi.io/cluster: kafka
spec:
  authentication:
    type: scram-sha-512
  authorization:
    type: simple
    acls:
      - resource:
          type: topic
          name: orders-topic
          patternType: literal
        operation: All
      - resource:
          type: group
          name: orders-group
          patternType: literal
        operation: All
```

📄 **payments-user.yaml**

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: KafkaUser
metadata:
  name: payments-app-user
  namespace: kafka
  labels:
    strimzi.io/cluster: kafka
spec:
  authentication:
    type: scram-sha-512
  authorization:
    type: simple
    acls:
      - resource:
          type: topic
          name: payments-topic
          patternType: literal
        operation: All
      - resource:
          type: group
          name: payments-group
          patternType: literal
        operation: All
```

---

## 7. Apply Order (CLI)

```bash
oc apply -f kafka.yaml -n kafka
oc apply -f broker-pool.yaml -n kafka
oc apply -f controller-pool.yaml -n kafka
oc apply -f orders-topic.yaml -n kafka
oc apply -f payments-topic.yaml -n kafka
oc apply -f orders-user.yaml -n kafka
oc apply -f payments-user.yaml -n kafka
```

---

## 8. Verify Deployment

* **Pods:**

  ```bash
  oc get pods -n kafka
  ```

  Expect:

  * 3 broker pods
  * 3 controller pods
  * 1 entity-operator pod

* **PVCs:**

  ```bash
  oc get pvc -n kafka
  ```

  Expect:

  * 3 × broker PVCs (100Gi)
  * 3 × controller PVCs (20Gi)

* **Services:**

  ```bash
  oc get svc -n kafka
  ```

  Look for:

  * `kafka-kafka-bootstrap` (internal)
  * `kafka-kafka-extscram-bootstrap` (LoadBalancer, port 9094)

---

## 9. Retrieve Connection Details

* **Bootstrap address:**

  ```bash
  oc -n kafka get svc kafka-kafka-extscram-bootstrap
  ```

  → use `EXTERNAL-HOSTNAME:9094`

* **User password (orders-app-user):**

  ```bash
  oc get secret orders-app-user -n kafka -o jsonpath='{.data.password}' | base64 -d
  ```

* **User password (payments-app-user):**

  ```bash
  oc get secret payments-app-user -n kafka -o jsonpath='{.data.password}' | base64 -d
  ```

---

## 10. Connect with Conduktor

1. Open **Conduktor → Add Cluster**.
2. Fill details:

   * **Bootstrap servers:** `EXTERNAL-HOSTNAME:9094`
   * **Security Protocol:** `SASL_SSL`
   * **SASL Mechanism:** `SCRAM-SHA-512`
   * **Username:** `orders-app-user` or `payments-app-user`
   * **Password:** from secret
   * **TLS/SSL:** enable **Skip SSL validation**
3. Save → **Test Connection**.

---

## 11. Access Validation

* `orders-app-user` → can access **orders-topic** only
* `payments-app-user` → can access **payments-topic** only
* Cross-topic access is denied (ACLs enforced)

