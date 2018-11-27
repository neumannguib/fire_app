
package br.pucrio.inf.lac.mhub_simulator;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.sql.Timestamp;
 
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;

/**
 * This class simulates packages from Mobile-Hubs.
 * @author Guilherme Neumann
 */
public class mhub implements NodeConnectionListener {
 
  private static String			gatewayIP   = "127.0.0.1";
  private static int			gatewayPort = 5500;
  private static MrUdpNodeConnection	connection;
 
  public static void main(String[] args) {
      Logger.getLogger("").setLevel(Level.OFF);
      //gatewayIP=args[0];
      //gatewayPort= Integer.parseInt(args[1]);
      //mhub mobilehub = new mhub(gatewayIP, gatewayPort);
      //mobilehub.newMessage(10,10);
      
  }
 
  public mhub(String gatewayIP, int gatewayPort) {

      InetSocketAddress address = new InetSocketAddress(gatewayIP, gatewayPort);
      try {
          connection = new MrUdpNodeConnection();
          connection.addNodeConnectionListener(this);
          connection.connect(address);
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
 
  public void connected(NodeConnection remoteCon) {
	  try {
		  ApplicationMessage message = new ApplicationMessage();
		  message.setPayloadType( PayloadSerialization.JSON );
		  Random number = new Random();
		  String serializableContent = "{" + 
						  "\"tag\":\"SensorData\"," + 
						  "\"uuid\":\""+connection.getUuid()+"\"," + 
						  "\"source\":\"00000000-0000-0000-0001-bc6a29aecef5\"," + 
						  "\"action\":\"read\"," + 
						  "\"signal\":-48," + 
						  "\"sensor_name\":\"Temperature\"," + 
						  "\"sensor_value\":["+number.nextFloat()*300 +"," + number.nextFloat()*300+ "]," + 
						  "\"latitude\":-22.799545," + 
						  "\"longitude\":-43.445945," + 
						  "\"timestamp\":1442169467" + 
						  "}";
			  System.out.println(serializableContent);
			  message.setContentObject(serializableContent);
			  connection.sendMessage(message);
			  
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
 
  public void newMessageReceived(NodeConnection remoteCon, Message message) {
      System.out.println(message.getContentObject());
  }
  
  public void newMessage (int data_sensor, int data_events) {
	  /**
	     * Method to send SensorData and EventData messages, in JSON format.It simulates packages from a real Mobile-Hub application.
	     * 
	     */
	  try {
		  
		  for (int j=0; j<data_sensor ;j++) {
				  ApplicationMessage message = new ApplicationMessage();
				  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				  message.setPayloadType( PayloadSerialization.JSON );
				  Random number = new Random();
				  String serializableContent = "{" + 
						  "\"tag\":\"SensorData\"," + 
						  "\"uuid\":\""+connection.getUuid()+"\"," + 
						  "\"source\":\"00000000-0000-0000-0001-bc6a29aecef5\"," + 
						  "\"action\":\"read\"," + 
						  "\"signal\":-48," + 
						  "\"sensor_name\":\"Temperature\"," + 
						  "\"sensor_value\":["+number.nextFloat()*300 +"," + number.nextFloat()*300+ "]," + 
						  "\"latitude\":-22.799545," + 
						  "\"longitude\":-43.445945," + 
						  "\"timestamp\":"+ timestamp.getTime()+
						  "}";
			  System.out.println(serializableContent);
			  message.setContentObject(serializableContent);
			  connection.sendMessage(message);
			  }
		  for (int i=0; i<data_events ;i++) {
			  ApplicationMessage message_event = new ApplicationMessage();
			  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			  message_event.setPayloadType( PayloadSerialization.JSON );
			  Random number1 = new Random();
			  String serializableContent1 = "{" + 
      		"\"tag\":\"EventData\"," + 
      		"\"uuid\":\""+connection.getUuid()+"\"," + 
      		"\"label\":\"MaxAVG\"," + 
      		"\"data\":{\"average_tmp\":"+ number1.nextFloat()*200+100 +"}," + 
      		"\"latitude\":-22.799545," + 
			 "\"longitude\":-43.445945," + 
      		"\"timestamp\":" + timestamp.getTime()+
      		"}";
			  System.out.println(serializableContent1);
			  message_event.setContentObject(serializableContent1);
    		  connection.sendMessage(message_event);
    		  try {
  				Thread.sleep(1000); //milliseconds to wait creation
  			} catch (InterruptedException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
			  Random number2 = new Random();
			  ApplicationMessage message_event2 = new ApplicationMessage();
			  Timestamp timestamp2 = new Timestamp(System.currentTimeMillis());
			  message_event2.setPayloadType( PayloadSerialization.JSON );
			  String serializableContent2 = "{" + 
      		"\"tag\":\"EventData\"," + 
      		"\"uuid\":\""+connection.getUuid()+"\"," + 
      		"\"label\":\"AVGHumidity\"," + 
      		"\"data\":{\"value_humi\":"+ number2.nextFloat()*200 +"}," + 
      		"\"latitude\":-22.799545," + 
			 "\"longitude\":-43.445945," + 
      		"\"timestamp\":" + timestamp2.getTime()+ 
      		"}";
			  
			  System.out.println(serializableContent2);
			  message_event2.setContentObject(serializableContent2);
    		  connection.sendMessage(message_event2);
    	  }
		  //connection.disconnect();
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
  public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {
	  System.out.printf("Node %s reconnected %d times\n",remoteCon.getUuid().toString(),remoteCon.getNumberOfReconnectionsMade());
  }
 
  public void disconnected(NodeConnection remoteCon) {
	  System.out.printf("Node %s disconnected\n", remoteCon.getUuid().toString());
  }
 
  public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {
	  System.out.printf("Unsent Messages: %d\n", unsentMessages.size());
  }
 
  public void internalException(NodeConnection remoteCon, Exception e) {
	  e.printStackTrace();
  }
}