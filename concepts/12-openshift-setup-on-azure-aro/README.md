# 🚀 How to Set Up OpenShift on Azure (ARO) – Using Azure Portal

## 1. Prerequisites

* **Azure Subscription** with enough **vCPU quota** (≈ 44 cores initially).
* **Permissions**: You must be **Owner** or **Contributor + User Access Administrator** on the subscription.
* **Red Hat Pull Secret** (recommended) – download from [cloud.redhat.com/openshift/install/azure](https://cloud.redhat.com/openshift/install/azure).
* Decide on **region, resource group, VNet, subnets, and cluster name**.

---

## 2. Setup Steps in Azure Portal

### Step 1: Register Resource Providers

1. Go to **Azure Portal** → **Subscriptions** → select your subscription.
2. Under **Resource Providers**, register:

   * `Microsoft.RedHatOpenShift`
   * `Microsoft.Compute`
   * `Microsoft.Storage`
   * `Microsoft.Authorization`

---

### Step 2: Create Networking

1. Go to **Virtual Networks** → **Create**.
2. Define:

   * Address space (e.g., `10.0.0.0/22`).
   * Two empty subnets:

     * `master-subnet` (`10.0.0.0/23`)
     * `worker-subnet` (`10.0.2.0/23`)
3. Save the VNet.

---

### Step 3: Create an ARO Cluster

1. In the portal search bar, type **Azure Red Hat OpenShift** → **Create**.
2. Fill in:

   * **Basics**: Subscription, Resource Group, Cluster Name, Region.
   * **Networking**: Select the VNet + the two subnets.
   * **Authentication**: Upload the Red Hat **pull secret**.
3. Review + Create → Deploy (takes \~35–45 mins).

---

### Step 4: Access the Cluster

1. Once provisioned, go to the cluster resource in the portal.
2. Copy the **OpenShift Console URL**.
3. Get the **kubeadmin credentials** from the portal → **Cluster Details → Credentials**.
4. Login to the OpenShift Web Console.

---

