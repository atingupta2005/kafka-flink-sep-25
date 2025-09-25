# Debugging & Troubleshooting — Streams for Apache Kafka on OpenShift

---

### Pods in CrashLoopBackOff

* Click pod → open **Logs**.
* Common errors:

  * *Wrong config in kafka.yaml* → check listener ports and replication configs.
  * *Not enough replicas to meet replication factor* → ensure at least 3 brokers and 3 controllers.

---

### External service not created

* Check **Networking → Services**.
* If no external LoadBalancer appears:

  * Your environment may not support LoadBalancer.
  * **Fix:** create an OpenShift Route or NodePort instead.

---

## Testing Issues

### Can’t connect from kafka-cli pod

* Ensure you used the **bootstrap service**: `my-cluster-kafka-bootstrap:9092`.
* If still failing, check that all controller and broker pods are running.

### Can’t connect from outside OpenShift

* Make sure you copied the **external IP/hostname** from the LoadBalancer service.
* If firewall blocks, open port 9094 in cloud security groups or on-prem firewall.

---

## Useful CLI for Debugging (optional)

If allowed to use CLI:

```
oc get ns
oc get po -n openshift-operators
oc logs -f amq-streams-cluster-operator-v3.0.1-1-58fdc64fc5-pc5lm -n openshift-operators
```

```bash
# check pods
oc get pods -n kafka

# check PVCs
oc get pvc -n kafka

# check services
oc get svc -n kafka

# describe resource for details
oc describe pod <pod-name> -n kafka
oc describe pvc <pvc-name> -n kafka
```

---

