subscriptions="cab7799e-bdd5-4b31-a003-c56612e38f3e"

readarray -d "," -t all_subscriptions <<< "$subscriptions"

az account show
locations="centralus,japaneast,northeurope,uksouth,southcentralus,westus,australiaeast,westus2,canadaeast,uaenorth,norwayeast,italinorth,koreacentral,canadacentral"

readarray -d "," -t all_locations <<< "$locations"

username="atingupta2005"
password="" # Replace with your desired password
vm_size="Standard_D2s_v5"
image="Win2022Datacenter"

# Define the number of VMs to create

num_vms=1

for current_subscription in ${all_subscriptions[@]}
do
 echo "Working on Subscription: $current_subscription"
 az account set --subscription $current_subscription
 for location in ${all_locations[@]}
 do
  echo "Working on location: $location"
  resource_group="rg-kafka-w-vms-$location"
  # echo az group create --name $resource_group --location $location
  az group create --name $resource_group --location $location
  for ((i=1; i<=$num_vms; i++)); do
      vm_name="vm-w-kafka$i"
      vm_dns_name="vm-w-jkp-kfk$i"

      # Create the VM
      az vm create --resource-group $resource_group --name $vm_name --image $image --admin-username $username --admin-password $password --location $location --size $vm_size --authentication-type password --storage-sku Standard_LRS --public-ip-sku Basic --public-ip-address-allocation static
  done
  
  az vm list -o table

 done
done
