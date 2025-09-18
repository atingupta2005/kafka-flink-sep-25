curl -fsSL https://get.docker.com -o install-docker.sh
sudo sh install-docker.sh
sudo usermod -aG docker $USER
exit

mkdir -p ~/.docker/cli-plugins/

curl -SL https://github.com/docker/compose/releases/download/v2.3.3/docker-compose-linux-x86_64 -o ~/.docker/cli-plugins/docker-compose

chmod +x ~/.docker/cli-plugins/docker-compose

docker compose version

cd ~

git clone https://github.com/atingupta2005/kafka-stack-docker-compose

# Replcae with public ip of your VM where kafka is being installed. Local VM as of now
VM_PUB_IP="13.73.201.28"

cd ~/kafka-stack-docker-compose

cat full-stack-zk-multiple-kafka-multiple-full-stack-ag.yml | grep EXTERNAL

sed -i "s/52.170.103.92/$VM_PUB_IP/g" full-stack-zk-multiple-kafka-multiple-full-stack-ag.yml

cat full-stack-zk-multiple-kafka-multiple-full-stack-ag.yml | grep EXTERNAL

docker compose -f full-stack-zk-multiple-kafka-multiple-full-stack-ag.yml up -d


curl localhost:8080

login:
admin@admin.io
admin
