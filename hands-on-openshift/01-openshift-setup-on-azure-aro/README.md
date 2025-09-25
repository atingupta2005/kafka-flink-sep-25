# How to Set Up OpenShift on Azure (ARO) – Using Azure Portal

---

## 1. Prerequisites

* **Azure Subscription** with enough vCPU quota (≈ 44 cores initially).
* **Permissions:** You must be *Owner* or *Contributor* + *User Access Administrator* on the subscription.
* **Red Hat Pull Secret** (recommended) – download from:
  [https://console.redhat.com/openshift/downloads#tool-pull-secret](https://console.redhat.com/openshift/downloads#tool-pull-secret)
* Decide on **region, resource group, VNet, subnets, and cluster name**.

---

## 2. Setup Steps in Azure Portal

### Step 1: Register Resource Providers

1. Go to **Azure Portal → Subscriptions → select your subscription**.
2. Under **Resource Providers**, register:

   * `Microsoft.RedHatOpenShift`
   * `Microsoft.Compute`
   * `Microsoft.Storage`
   * `Microsoft.Authorization`

### Step 2: Create an ARO Cluster

1. In the portal search bar, type **Azure Red Hat OpenShift → Create**.
2. Fill in:

   * **Basics:** Subscription, Resource Group, Cluster Name, Region.
   * **Networking:** Select the VNet + the two subnets.
   * **Authentication:** Upload the Red Hat pull secret.
3. **Review + Create → Deploy** (takes \~35–45 mins).

### Step 3: Access the Cluster

1. Once provisioned, go to the **cluster resource** in the portal.
2. Copy the **OpenShift Console URL**.
3. Get the **kubeadmin credentials** from:
   Portal → Cluster Details → **Credentials**.
4. Login to the OpenShift Web Console.

---

## 3. Access the Cluster with `oc` CLI

Install `oc` CLI if not already installed:
👉 Download from: [https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/](https://mirror.openshift.com/pub/openshift-v4/clients/ocp/latest/)

### Login using kubeadmin (from portal)

```bash
# Example (replace with your real console URL)
oc login https://api.<cluster-name>.<region>.aroapp.io:6443 \
  -u kubeadmin \
  -p <password-from-portal>
```

### Login using your OpenShift token

1. Go to the OpenShift Web Console.
2. Click your user name (top right) → **Copy Login Command**.
3. Copy the `oc login --token=...` command and run it in your terminal.

Example:

```bash
oc login --token=sha256~xxxxxxxxxxxx \
  --server=https://api.<cluster-name>.<region>.aroapp.io:6443
```

---

## 4. Verify Cluster Health via CLI

### Check cluster info

```bash
oc whoami
oc cluster-info
oc version
```

### Check nodes

```bash
oc get nodes
oc describe node <node-name>
```

* Nodes should be **Ready**.

### Check cluster operators

```bash
oc get co
```

* All should be **Available=True**, **Progressing=False**, **Degraded=False**.

### Check pods across namespaces

```bash
oc get pods -A
```

* Look for pods stuck in `CrashLoopBackOff` or `Pending`.

### Check events

```bash
oc get events -A --sort-by=.metadata.creationTimestamp
```

---

## 5. Common Troubleshooting

* **Login fails** → Check API server URL and token/credentials.
* **Nodes not Ready** → Run `oc describe node` to see resource or scheduling issues.
* **Operators degraded** → Run `oc get co` and `oc describe co <operator-name>`.
* **Pods failing** → Run `oc logs pod/<name> -n <namespace>`.

