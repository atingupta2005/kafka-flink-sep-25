Flink Kubernetes Operator
1.12.0 provided by Community

-----------------------------------------------

apiVersion: flink.apache.org/v1beta1
kind: FlinkDeployment
metadata:
  creationTimestamp: '2025-09-24T14:40:49Z'
  finalizers:
    - flinkdeployments.flink.apache.org/finalizer
  generation: 3
  managedFields:
    - apiVersion: flink.apache.org/v1beta1
      fieldsType: FieldsV1
      fieldsV1:
        'f:spec':
          .: {}
          'f:flinkConfiguration':
            .: {}
            'f:taskmanager.numberOfTaskSlots': {}
          'f:flinkVersion': {}
          'f:image': {}
          'f:jobManager':
            .: {}
            'f:replicas': {}
            'f:resource':
              .: {}
              'f:cpu': {}
              'f:memory': {}
          'f:taskManager':
            .: {}
            'f:replicas': {}
            'f:resource':
              .: {}
              'f:cpu': {}
              'f:memory': {}
      manager: Mozilla
      operation: Update
      time: '2025-09-24T14:40:49Z'
    - apiVersion: flink.apache.org/v1beta1
      fieldsType: FieldsV1
      fieldsV1:
        'f:metadata':
          'f:finalizers':
            .: {}
            'v:"flinkdeployments.flink.apache.org/finalizer"': {}
      manager: fabric8-kubernetes-client
      operation: Update
      time: '2025-09-24T14:40:49Z'
    - apiVersion: flink.apache.org/v1beta1
      fieldsType: FieldsV1
      fieldsV1:
        'f:spec':
          'f:serviceAccount': {}
      manager: kubectl-patch
      operation: Update
      time: '2025-09-24T14:57:49Z'
    - apiVersion: flink.apache.org/v1beta1
      fieldsType: FieldsV1
      fieldsV1:
        'f:status':
          .: {}
          'f:clusterInfo':
            .: {}
            'f:flink-revision': {}
            'f:flink-version': {}
            'f:total-cpu': {}
            'f:total-memory': {}
          'f:jobManagerDeploymentStatus': {}
          'f:jobStatus':
            .: {}
            'f:checkpointInfo':
              .: {}
              'f:lastPeriodicCheckpointTimestamp': {}
            'f:savepointInfo':
              .: {}
              'f:lastPeriodicSavepointTimestamp': {}
              'f:savepointHistory': {}
            'f:state': {}
          'f:lifecycleState': {}
          'f:observedGeneration': {}
          'f:reconciliationStatus':
            .: {}
            'f:lastReconciledSpec': {}
            'f:lastStableSpec': {}
            'f:reconciliationTimestamp': {}
            'f:state': {}
      manager: fabric8-kubernetes-client
      operation: Update
      subresource: status
      time: '2025-09-24T14:58:30Z'
  name: flink-session
  namespace: flink
  resourceVersion: '143729'
  uid: 91339b0a-b52d-47ca-81b1-fd475b2e5712
spec:
  flinkConfiguration:
    taskmanager.numberOfTaskSlots: '2'
  flinkVersion: v1_17
  image: 'flink:1.17'
  jobManager:
    replicas: 1
    resource:
      cpu: 1
      memory: 2048m
  serviceAccount: flink
  taskManager:
    replicas: 2
    resource:
      cpu: 1
      memory: 2048m
status:
  clusterInfo:
    flink-revision: 'c0027e5 @ 2023-11-09T13:24:38+01:00'
    flink-version: 1.17.2
    total-cpu: '1.0'
    total-memory: '2147483648'
  jobManagerDeploymentStatus: READY
  jobStatus:
    checkpointInfo:
      lastPeriodicCheckpointTimestamp: 0
    savepointInfo:
      lastPeriodicSavepointTimestamp: 0
      savepointHistory: []
    state: FINISHED
  lifecycleState: STABLE
  observedGeneration: 3
  reconciliationStatus:
    lastReconciledSpec: '{"spec":{"job":null,"restartNonce":null,"flinkConfiguration":{"taskmanager.numberOfTaskSlots":"2"},"image":"flink:1.17","imagePullPolicy":null,"serviceAccount":"flink","flinkVersion":"v1_17","ingress":null,"podTemplate":null,"jobManager":{"resource":{"cpu":1.0,"memory":"2048m","ephemeralStorage":null},"replicas":1,"podTemplate":null},"taskManager":{"resource":{"cpu":1.0,"memory":"2048m","ephemeralStorage":null},"replicas":2,"podTemplate":null},"logConfiguration":null,"mode":null},"resource_metadata":{"apiVersion":"flink.apache.org/v1beta1","firstDeployment":false}}'
    lastStableSpec: '{"spec":{"job":null,"restartNonce":null,"flinkConfiguration":{"taskmanager.numberOfTaskSlots":"2"},"image":"flink:1.17","imagePullPolicy":null,"serviceAccount":"flink","flinkVersion":"v1_17","ingress":null,"podTemplate":null,"jobManager":{"resource":{"cpu":1.0,"memory":"2048m","ephemeralStorage":null},"replicas":1,"podTemplate":null},"taskManager":{"resource":{"cpu":1.0,"memory":"2048m","ephemeralStorage":null},"replicas":2,"podTemplate":null},"logConfiguration":null,"mode":null},"resource_metadata":{"apiVersion":"flink.apache.org/v1beta1","firstDeployment":false}}'
    reconciliationTimestamp: 1758725892720
    state: DEPLOYED

-----------------------------------------------
CLI Commands:

 104  oc expose svc/flink-session-rest -n flink
  105  oc get route -n flink
  106  oc patch svc flink-session-rest -n flink -p '{"spec": {"type": "LoadBalancer"}}'
  107  oc get svc -n flink
  108  oc get svc -n flink
  109  oc get pods -n flink
  110  curl http://4.198.95.213:8081
  111  oc expose svc flink-session-rest -n flink
  112  oc get route -n flink
  113  oc describe svc flink-session-rest -n flink
  114  oc get pods -n flink -o wide
  115  oc describe pods flink-session-6759df8c5c-fbcn5~  -n flink-o wide
  116  oc describe pods flink-session-6759df8c5c-fbcn5  -n flink-o wide
  117  oc describe pods flink-session-6759df8c5c-fbcn5  -n flink
  118  oc get logs pods flink-session-6759df8c5c-fbcn5  -n flink
  119  oc get log pods flink-session-6759df8c5c-fbcn5  -n flink
  120  oc logs flink-session-6759df8c5c-fbcn5 -n flink --previous
  121  oc get pods -n flink -o wide
  122  oc create sa flink -n flink
  123  oc adm policy add-role-to-user edit system:serviceaccount:flink:flink -n flink
  124  oc patch flinkdeployment flink-session   -n flink   --type=merge   -p '{"spec":{"serviceAccount":"flink"}}'
  125  oc get pods -n flink
  126  oc get pods -n flink
  127  oc get events -n flink
  128  oc get pods -n flink
  129  oc get akk -n flink
  130  oc get all -n flink
  131  oc get all -n flink
  132  oc get events -n flink
  133  oc get pods -n flink
  134  oc get log pod/flink-session-55d9bc87b8-lnj27 -n flink
  135  oc log pod/flink-session-55d9bc87b8-lnj27 -n flink
  136  oc logs pod/flink-session-55d9bc87b8-lnj27 -n flink
  137  oc logs -f pod/flink-session-55d9bc87b8-lnj27 -n flink
  138  oc get pods -n flink
  139  oc get all  -n flink
  140  oc logs -f pod/flink-session-55d9bc87b8-lnj27 -n flink
  141  oc get pods -n flink
  142  oc get all  -n flink
  143  kubectl expose service/flink-session-rest -n flask
  144  oc expose service/flink-session-rest -n flask
  145  oc expose service/flink-session-rest -n flink
  146  oc get all  -n flink
  147  oc get flinkdeployment flink-session -n flink -o yaml | grep -A6 "taskManager:"
  148  oc get pods -n flink
  149  oc get pods -n flink
  150  oc get pods -n flink
  151  oc get pods -n flink
  152  history



Worked URL for Flink UI:
http://flink-session-rest-flink.apps.umfxusb0f35426db2c.australiasoutheast.aroapp.io/#/overview