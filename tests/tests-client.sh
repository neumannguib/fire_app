echo "What's the experiment number:"
read experiment
echo "Experiment $experiment starting ..."
echo "Using Open-jdk-8"
echo "What's the number of mhubs:"
read mhub
echo "What's the number of mobjects:"
read obj

export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH
cd /home/neumannguib/eclipse-workspace/Fire_app/jar/


nohup java -jar supervisor.jar 127.0.0.1 5500 > ../Logs/Experiment$experiment/supervisor.log &

nohup java -jar fire_department.jar 127.0.0.1 5500 > ../Logs/Experiment$experiment/fire_department.log &

nohup java -jar mhub-simulator.jar 127.0.0.1 5500 $mhub $obj $obj > ../Logs/Experiment$experiment/mhubs.log &
