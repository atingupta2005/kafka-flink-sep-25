# Install Apache Flink on OpenShift using Flink Kubernetes Operator

## Install the Operator

1. Log in to OpenShift Web Console.
2. Go to **Operators → OperatorHub**.
3. Search for **Flink Kubernetes Operator** (Community).
4. Click → **Install**.
5. Choose:

   * **Installation Mode**: All namespaces on the cluster
   * **Installed Namespace**: `openshift-operators`
   * **Approval**: Automatic
6. Click **Subscribe**.
7. Go to **Operators → Installed Operators**.

   * Wait until status = **Succeeded**.

---

## Create a Project

```bash
oc new-project flink
```

---

## Give Permissions to Default ServiceAccount

```bash
oc adm policy add-role-to-user edit system:serviceaccount:flink:default -n flink
```

---

## Deploy a Flink Cluster

Create `flink-deployment.yaml`:

```yaml
apiVersion: flink.apache.org/v1beta1
kind: FlinkDeployment
metadata:
  name: flink-standalone
  namespace: flink
spec:
  mode: standalone
  flinkVersion: v1_17
  image: flink:1.17.2-scala_2.12
  flinkConfiguration:
    taskmanager.numberOfTaskSlots: "2"
  jobManager:
    replicas: 1
    resource:
      cpu: 1
      memory: 2048m
  taskManager:
    replicas: 2
    resource:
      cpu: 1
      memory: 2048m
  serviceAccount: default
```

Apply:

```bash
oc apply -f flink-deployment.yaml
```

---

## Verify Deployment

```bash
oc get pods -n flink
oc get flinkdeployment flink-standalone -n flink -o yaml | grep lifecycleState:
```

* Expect 1 **JobManager pod** + 2 **TaskManager pods**.
* Status should show: `STABLE`.

---

## Access Flink UI

Expose service:

```bash
oc get svc -n flink
oc expose svc flink-standalone-rest -n flink
oc get route -n flink
```

Open the route in your browser → Flink Web Dashboard.

---

# Debugging & Troubleshooting

---

## Pods & Logs

```bash
oc get pods -n flink
oc describe pod <pod> -n flink
oc logs <pod> -n flink --tail=100 -f
```

---

## Events

```bash
oc get events -n flink --sort-by=.metadata.creationTimestamp | tail -n 50
```

---

## Operator Logs

```bash
oc logs -n openshift-operators deploy/flink-kubernetes-operator --tail=100 -f
```

---

## FlinkDeployment Status

```bash
oc get flinkdeployment -n flink
oc describe flinkdeployment flink-standalone -n flink
oc get flinkdeployment flink-standalone -n flink -o yaml
```

Look at `.status.lifecycleState` (`STABLE`, `DEPLOYING`, `FAILED`).

---

## Permissions Debug

```bash
oc get rolebinding -n flink
oc get sa -n flink
```

If missing permissions:

```bash
oc adm policy add-role-to-user edit system:serviceaccount:flink:default -n flink
```

---

## Network & UI Debug

```bash
oc get svc -n flink
oc describe svc flink-standalone-rest -n flink
oc get route -n flink
curl http://<flink-route>/config
```

---

## Common Issues

* **Pods Pending** → Not enough CPU/memory. Adjust resources.
* **Startup probe failed** → Wrong image. Use official Flink images.
* **Permission denied** → SA missing `edit` role.
* **UI not reachable** → Route missing; recreate with `oc expose`.
* **No TaskManagers** → Check `taskManager.replicas`, operator logs, and pod events.

---

## Cleanup

```bash
oc delete flinkdeployment flink-standalone -n flink
oc delete project flink
```
