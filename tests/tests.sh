echo "What's the experiment number:"
read $experiment
echo "Experiment $experiment starting ..."
echo "Using Open-jdk-8"


export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH
cd /home/neumannguib/eclipse-workspace/Fire_app/resources/
nohup java -jar contextnet-2.5.jar 127.0.0.1 5500 OpenSplice &
echo "Gateway running"
cd ../jar 

mkdir ../Logs/Experiment$experiment

echo "Server starting, logs at home/neumannguib/eclipse-workspace/Fire_app/Logs/Experiment$experiment"

java -jar server.jar localhost 5432 Fire_db > ../Logs/Experiment$experiment/server.log <<inputserver
SFS
adminadmin
100
300
35
r
inputserver

