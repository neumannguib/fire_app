/**
 * 
 */
package br.pucrio.inf.lac.tests;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.*;

import br.pucrio.inf.lac.CoreServer;
import br.pucrio.inf.lac.database_connection;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;
import lac.cnclib.sddl.message.ClientLibProtocol.UUID;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.Message;
import lac.cnet.sddl.objects.ApplicationObject;

/**
 * @author Guilherme Neumann
 *
 */
public class CoreServerTest {

	/**
	 * Test method for {@link br.pucrio.inf.lac.CoreServer#sendBroadcastMSG(lac.cnclib.sddl.message.ApplicationMessage)}.
	 * @throws SQLException 
	 */
	
	public static void main( String[] args ) throws SQLException {
		CoreServerTest test =new CoreServerTest();
		test.testHandleEvent(); // to run that it needed to be set bd user and password in CoreServer
	}
	@Test
	public void testHandleEvent() throws SQLException {
		Random number = new Random();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		java.util.UUID nodeId=java.util.UUID.randomUUID();
		String serializableContent = "{\"average_tmp\":"+ number.nextFloat()*200+100 +"}";
		JSONParser parser = new JSONParser();
		try {
			JSONObject object = (JSONObject) parser.parse( serializableContent );
			CoreServer server = new CoreServer();
			server.handleEvent("MaxAVG", object,-22.799545, -43.445945, timestamp, nodeId);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	
	@Test
	public void testOnNewData() {
		ApplicationMessage msg = new ApplicationMessage();
		msg.setSenderID(java.util.UUID.randomUUID());
		msg.setPayloadType( PayloadSerialization.JSON );
		Random number = new Random();
		String serializableContent = "{" + 
						  "\"tag\":\"SensorData\"," + 
						  "\"uuid\":\""+msg.getSenderID()+"\"," + 
						  "\"source\":\"00000000-0000-0000-0002-bc6a29aecef5\"," + 
						  "\"action\":\"read\"," + 
						  "\"signal\":-48," + 
						  "\"sensor_name\":\"Temperature\"," + 
						  "\"sensor_value\":["+number.nextFloat()*300 +"," + number.nextFloat()*300+ "]," + 
						  "\"latitude\":-22.799545," + 
						  "\"longitude\":-43.445945," + 
						  "\"timestamp\":1442169467" + 
						  "}";
		msg.setContentObject(serializableContent);
		Object message = (Object)msg;
		ApplicationObject topicSample= (ApplicationObject) msg; 
		CoreServer server = new CoreServer();
		server.onNewData(topicSample);
		Statement st,st1,st2;
		Connection conn = new database_connection().database_connection("localhost","5432","Fire_db","SFS","adminadmin"); 
		 /**Test mhub UUID in DB*/
		try {
			st1 = conn.createStatement();
			ResultSet result = st1.executeQuery("SELECT uuid FROM mhub WHERE uuid='"+msg.getSenderID()+"';");
			assertTrue(result.next());
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		/**Test source id in DB*/
		try {
			st2 = conn.createStatement();
			ResultSet result = st2.executeQuery("SELECT uuid FROM sensor WHERE uuid='00000000-0000-0000-0002-bc6a29aecef5';");
			assertTrue(result.next());
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		/**Test Sensor Data in DB after 1000 messages*/
//		ApplicationMessage msg1 = new ApplicationMessage();
//		for(int i=0;i<1000;i++) {
//			msg1.setSenderID(java.util.UUID.randomUUID());
//			msg1.setPayloadType( PayloadSerialization.JSON );
//			Random number1 = new Random();
//			String serializableContent1 = "{" + 
//							  "\"tag\":\"SensorData\"," + 
//							  "\"uuid\":\""+msg1.getSenderID()+"\"," + 
//							  "\"source\":\"00000000-0000-0000-0002-bc6a29aecef5\"," + 
//							  "\"action\":\"read\"," + 
//							  "\"signal\":-48," + 
//							  "\"sensor_name\":\"Temperature\"," + 
//							  "\"sensor_value\":["+number1.nextFloat()*300 +"," + number1.nextFloat()*300+ "]," + 
//							  "\"latitude\":-22.799545," + 
//							  "\"longitude\":-43.445945," + 
//							  "\"timestamp\":1442169467" + 
//							  "}";
//			msg1.setContentObject(serializableContent1);
//			ApplicationObject topicSample1=(ApplicationObject) msg1;
//			server.onNewData(topicSample1);
//		}
//		try {
//			st = conn.createStatement();
//			ResultSet result = st.executeQuery("SELECT mhub FROM sensor_data WHERE uuid='"+msg1.getSenderID()+"';");
//			assertTrue(result.next());
//			
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} 
	}

}
