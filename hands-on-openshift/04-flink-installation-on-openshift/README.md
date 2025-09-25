# Install Apache Flink on OpenShift using Flink Kubernetes Operator

This guide shows how to deploy **Apache Flink 1.17** on OpenShift using the **Flink Kubernetes Operator (1.12.0, community)**.
We’ll install the operator, deploy a **Flink Session Cluster**, open the Flink UI, and cover basic troubleshooting.

---

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

1. Go to **Home → Projects → Create Project**.
2. Name it: `flink`.
3. Switch to the `flink` project.

---

## Give Permissions to Default ServiceAccount

```bash
oc adm policy add-role-to-user edit system:serviceaccount:flink:default -n flink
```

---

## Deploy a Flink Cluster

1. Go to **Operators → Installed Operators → Flink Kubernetes Operator → FlinkDeployment**.
2. Click **Create → YAML view**.
3. Paste this YAML:

```yaml
apiVersion: flink.apache.org/v1beta1
kind: FlinkDeployment
metadata:
  name: flink-session
  namespace: flink
spec:
  image: flink:1.17
  flinkVersion: v1_17
  serviceAccount: default
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
  flinkConfiguration:
    taskmanager.numberOfTaskSlots: "2"
```

4. Click **Create**.

---

## Verify Deployment

* Go to **Workloads → Pods** in `flink` project:

  * Expect 1 JobManager pod, 2 TaskManager pods.
  * All should be **Running**.

* Go to **Operators → Flink Kubernetes Operator → FlinkDeployment**.

  * Click `flink-session` → status should show **READY** or **STABLE**.

---

## Access Flink UI

1. In **Networking → Services**, find `flink-session-rest`.
2. Click **Actions → Create Route**.

   * Name: `flink-ui`
   * Target Port: `8081`
3. Save.
4. Copy the Route URL and open in browser → Flink Web Dashboard.

---

## Test Flink

1. Go to the Flink Web UI.
2. Upload a job JAR
3. Submit and run → monitor via UI.

---

# Debugging & Troubleshooting

---

## Check Pods

```bash
oc get pods -n flink
oc describe pod <pod-name> -n flink
oc logs pod/<pod-name> -n flink
```

* **CrashLoopBackOff** → check container logs.
* **Pending** → check events (`oc describe pod`) → may be storage or resource issue.

---

## Check Services & Routes

```bash
oc get svc -n flink
oc describe svc flink-session-rest -n flink
oc get route -n flink
```

* If no external route → create manually:

  ```bash
  oc expose svc flink-session-rest -n flink
  oc get route -n flink
  ```

---

## Debug Permission Issues

* Check RoleBindings in the project:

```bash
oc get rolebinding -n flink
```

* If you missed giving `edit` role to default SA, fix:

```bash
oc adm policy add-role-to-user edit system:serviceaccount:flink:default -n flink
```

* If still errors, patch the FlinkDeployment to set serviceAccount explicitly:

```bash
oc patch flinkdeployment flink-session -n flink --type=merge -p '{"spec":{"serviceAccount":"default"}}'
```

---

## Debug FlinkDeployment

```bash
oc get flinkdeployment -n flink
oc describe flinkdeployment flink-session -n flink
oc get flinkdeployment flink-session -n flink -o yaml
```

* Look at `status.lifecycleState` → should be `STABLE`.
* If `DEPLOY_FAILED`, check JobManager pod logs.

---

## Common Problems

* **Pods Pending** → Not enough CPU/memory, or storage class not available.
* **Permission denied** → Default SA missing `edit` role. Fix with `oc adm policy add-role-to-user`.
* **UI not reachable** → Route missing. Use `oc expose svc flink-session-rest -n flink`.
* **Job stuck** → Check TaskManager pod logs.

---

## Cleanup

```bash
oc delete flinkdeployment flink-session -n flink
oc delete project flink
```
