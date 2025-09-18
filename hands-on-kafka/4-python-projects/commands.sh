sudo rm -rf ~/kafka-ey-24
cd ~
git clone https://github.com/atingupta2005/kafka-ey-24
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects
sudo  apt install python3.8-venv
#sudo python3 -m venv /pyenv
sudo chmod -R a+rwx /pyenv
deactivate
source /pyenv/bin/activate
nc -vz localhost 2181
nc -vz localhost 9092
sudo apt install httpie  -y

USER_SUFFIX=$(echo "$USER" | cut -c 2-5)
echo $USER_SUFFIX

##----------------------------------------------------------------##
# Create Topic
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/1-python-kafka-admin-api-simple
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 103$USER_SUFFIX > output.log 2>&1 &
sleep 2
curl localhost:103$USER_SUFFIX/hello-world
ps -x | grep "/pyenv/bin/python3" | grep -v grep | awk '{print $1}' | xargs -I {} sh -c 'kill -9 $(pgrep -P {}) && kill -9 {}'
##----------------------------------------------------------------##
# Create Topic with 'retention.ms': '360000'
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/2-python-kafka-admin-api-adv
pip install -r requirements.txt

uvicorn main:app --reload --host 0.0.0.0 --port 103$USER_SUFFIX > output.log 2>&1 &
sleep 2
curl localhost:103$USER_SUFFIX/hello-world
ps -x | grep "/pyenv/bin/python3" | grep -v grep | awk '{print $1}' | xargs -I {} sh -c 'kill -9 $(pgrep -P {}) && kill -9 {}'
##----------------------------------------------------------------##
# Create message when we call api using POST - /api/people
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/5-peopleservice-basic-producer
pip install -r requirements.txt

uvicorn main:app --reload --host 0.0.0.0 --port 103$USER_SUFFIX > output.log 2>&1 &
sleep 2
http POST :103$USER_SUFFIX/api/people count:=5
ps -x | grep "/pyenv/bin/python3" | grep -v grep | awk '{print $1}' | xargs -I {} sh -c 'kill -9 $(pgrep -P {}) && kill -9 {}'
##----------------------------------------------------------------##
# Create message when we call api using POST - /api/people with callbacks
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/8-peopleservice-producer-python-adv
pip install -r requirements.txt

uvicorn main:app --reload --host 0.0.0.0 --port 103$USER_SUFFIX > output.log 2>&1 &
sleep 2
http POST :103$USER_SUFFIX/api/people count:=5
tail output.log
##----------------------------------------------------------------##
# Kafka consumer
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/10-peopleconsumeradv
pip install -r requirements.txt

python pyconsumer.py &
sleep 2
http POST :103$USER_SUFFIX/api/people count:=1
sudo pkill uvicorn
ps -x | grep "/pyenv/bin/python3" | grep -v grep | awk '{print $1}' | xargs -I {} sh -c 'kill -9 $(pgrep -P {}) && kill -9 {}'
##----------------------------------------------------------------##
# Produce message with AVRO serialization
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/15-avro-people-service-producer
pip install -r requirements.txt

uvicorn main:app --reload --host 0.0.0.0 --port 103$USER_SUFFIX > output.log 2>&1 &
sleep 2
http POST :103$USER_SUFFIX/api/people count:=5
##----------------------------------------------------------------##
# Kafka consumer with AVRO de-serialization
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/16-avro-people-service-consumer
pip install -r requirements.txt

python pyconsumer.py &
sleep 2
http POST :103$USER_SUFFIX/api/people count:=1
sudo pkill uvicorn
ps -x | grep "/pyenv/bin/python3" | grep -v grep | awk '{print $1}' | xargs -I {} sh -c 'kill -9 $(pgrep -P {}) && kill -9 {}'
##----------------------------------------------------------------##
# Kafka producer and consumer with AVRO and schema evolution
cd ~/kafka-ey-24/hands-on-kafka/2-python-projects/17-avro-people-service-schema-evolution
pip install -r requirements.txt

uvicorn main:app --reload --host 0.0.0.0 --port 103$USER_SUFFIX > output.log 2>&1 &
sleep 2
http POST :103$USER_SUFFIX/api/people count:=1
python pyconsumer.py &
http POST :103$USER_SUFFIX/api/people count:=10
sudo pkill uvicorn
ps -x | grep "/pyenv/bin/python3" | grep -v grep | awk '{print $1}' | xargs -I {} sh -c 'kill -9 $(pgrep -P {}) && kill -9 {}'
##----------------------------------------------------------------##
