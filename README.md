# fire_app

Firstly, you need jdk 8. Unfortunately, more recent java versions do not support the middleware
ContextNet [1] we use. So, even if you have a more recent version, before running our application,
export jdk-8 as your environment variable:

export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:$PATH

You also will need a DDS (Data Distribution Service), such as Open Splice, an open source DDS
used by us, to run ContextNet. For a complete guide to Open Splice installation, visit:
http://wiki.lac.inf.puc-rio.br/doku.php?id=installingdds .

We use a Database to store all event alerts, mobile hub and mobile object data. For that reason, you
will need to have PostgreSQL installed (https://www.postgresql.org/download/). After that
installation, you can restore our DB, with the schema needed. It is in resources directory.
Throughout pgAdminIII interface, you can restore it clicking with right bottom in the database icon
(we call it Fire_db), and then click on Restore. Search for Fire-db_bkp.backup file and then Restore
it. 
The schema is composed of four tables, which contains Mobile Hub info, mhub table, sensor data
info, sensor_data table, fire and fire risk events info, events table, and mobile objects info, sensor
table.


You do not need to perform any installation to use our System. It is composed of some jar files, as
follows:

- contextnet-2.5.jar or contextnet-2.7.jar to run the gateway component, through Open Splice, using
Open Splice version 2.4, and version 2.6, respectively. For that, use the following command
changing the ip and port parameters. Remember to use jdk-8. This file is into resources directory.

java -jar contextnet-2.5.jar 127.0.0.1 5500 OpenSplice

- server.jar. To run the Smart Forest Server (SFS), you basically need to run the server.jar file (in the
same machine you have run the gateway), stored in the jar directory. One more time, remember to
use jdk-8. It will ask you three important parameters, the “Maximum temperature to inform
Supervisor”, the “Maximum temperature to inform Fire Department” and the “Minimum humidity
to inform Fire Risk”. In general, we use 100, 300 and 35, respectively. Which means, temperatures
above 100ºC will generate Fire alert to Park Supervisor, temperatures above 300ºC will also
generate fire alerts to Fire Department, and humidity values below 35% will generate fire risk alerts
to Park Supervisor. The java command requires database connection information, ordered as ip, port
(normally 5432), and database name. Before connection, SFS will ask your login and password for
database. We used SFS user and adminadmin password. You can change it.

java -jar server.jar localhost 5432 Fire_db

- supervisor.jar and fire_department.jar . Again, using jdk-8, we can launch Supervisor and Fire
Department applications using their jar files, and IP and port from SFS configured in the gateway
component (contextnet-2.5.jar or contextnet-2.7.jar).

java -jar supervisor.jar 127.0.0.1 5500

- MobileHub-v1.2.apk. You can install the apk file to start MobileHub app in your phone. There you
will insert connection details for SFS. For more details visit: http://wiki.lac.inf.puc-rio.br/doku.php?
id=m_hub.
M-Hubs executes in Android System with four java threads: The Connection Server that executes
MR-UDP, sending, receiving and buffering messages; the Sensory, Presence and Actuation (S2PA)
Service responsible for connecting mobile objects (Sensor Tags) through WPAN technologies; the
Location Service that uses GPS information to notify Connection Server the location; and the
Energy Manager, that regulates the operation frequency to other threads depending on the device
energy level. In M-Hubs there is an agent from CEP called Mobile-EPA (Event Processing Agent),
using the language Asper, responsible for the processing in Edge Computing.

- mhub-simulator.jar. Before using the apk file in your phone, you can previously test the server
with mhub-simulator.jar. It also requires jdk-8. Use ip, port, number of mobile hubs, number of
sensor data, and number of event data to run that.

 java -jar mhub-simulator.jar 127.0.0.1 5500 10 3 3
 
[1]M. Endler, G. Baptista, L. D. Silva, R. Vasconcelos, M. Malcher, V. Pantoja, V. Pinheiro, and J.
Viterbo,“ContextNet: Context Reasoning and Sharing Middleware for Large-scale Pervasive
Collaboration and Social Networking,” ACM/USENIX Middlew. Conf., 2011
 
For any other specific problems do not hesitate in writing us through gneumann@inf.puc-rio.br or
through an issue at https://github.com/neumannguib/fire_app.

You can also read our article at:
https://wocces2018.weebly.com/uploads/1/1/7/9/117971898/wocces2018_paper8_short.pdf
