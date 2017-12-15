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
 
public class FireDepartment implements NodeConnectionListener {
 
  private static String			gatewayIP   = "139.82.153.221";
  private static int			gatewayPort = 5500;
  private MrUdpNodeConnection	connection;
 
  public static void main(String[] args) {
      Logger.getLogger("").setLevel(Level.OFF);
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


