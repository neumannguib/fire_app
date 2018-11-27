package br.pucrio.inf.lac;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
 

/**
 * This class defines the client "Fire Department". It basically receives messages regarding fire detection. 
 * @author Guilherme Neumann
 *  */

public class FireDepartment implements NodeConnectionListener {
 
  private static String			gatewayIP   = "127.0.0.1";
  private static int			gatewayPort = 5500;
  private MrUdpNodeConnection	connection;
 
  public static void main(String[] args) {
      Logger.getLogger("").setLevel(Level.OFF);
      if(args.length!=0) {
    	  gatewayIP=args[0];
    	  gatewayPort= Integer.parseInt(args[1]);
      }
      
      new FireDepartment();
  }
 
  public FireDepartment() {
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
      ApplicationMessage message = new ApplicationMessage();
      String serializableContent = "Fire Department Listening ...";
      System.out.println(serializableContent);
      System.out.printf("%s:%d \n",gatewayIP, gatewayPort);
      message.setContentObject(serializableContent);
       
      try {
          connection.sendMessage(message);
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
 
  public void newMessageReceived(NodeConnection remoteCon, Message message) {
      System.out.println(message.getContentObject());
  }
  // other methods
  
  public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {}
 
  public void disconnected(NodeConnection remoteCon) {}
 
  public void unsentMessages(NodeConnection remoteCon, List<Message> unsentMessages) {}
 
  public void internalException(NodeConnection remoteCon, Exception e) {}
}


