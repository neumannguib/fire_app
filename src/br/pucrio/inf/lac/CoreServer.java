package br.pucrio.inf.lac;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.sql.*;
import java.text.SimpleDateFormat;
import com.esri.core.geometry.*;


import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;
import lac.cnclib.sddl.serialization.Serialization;
import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory.SupportedDDSVendors;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;
/** 
 * Server main class. It connects with and receives data from mobile hubs, process the data, store that in a database and handle fire risk and fire event packages.
 * 
 * @author Guilherme Neumann
 *
 */
public class CoreServer implements UDIDataReaderListener<ApplicationObject> {
	
	/** SDDL Elements */
    private Object receiveMessageTopic;
    private Object toMobileNodeTopic;
    private static SddlLayer core;
    
    /** Mobile Objects Data */
    private static final Map<UUID, UUID> mMobileHubs = new HashMap<>();
    private static final ArrayList<String> sensors = new ArrayList<String>();
    
    /** Connection to the database server */
    private static String ip="localhost";
    private static String port="5432";
    private static String db="Fire_db";
    private static String user;
    private static String password;
  	Connection conn = new database_connection().database_connection(ip,port,db,user,password); 
    
    /** Input reader */
    private static Scanner sc = new Scanner( System.in );
    
    /** Clients ID */
    private static UUID nodeFire=null; 
    private static UUID nodeSupervisor=null;
    
    /** Auxiliary variables*/
    public static int events=0;
    public static int sensor_temp=0;
    public static int sensor_humi=0;
    public static String message[] = new String[3];
    public static double maxtemp;
    public static double maxtemp_fire;
    public static double minhumi;
    
    
	public static void main( String[] args ) {
		if(args.length!=0) {
			ip=args[0];
			port=args[1];
			db=args[2];
		}
		
		System.out.println("Database login\nUser:");
		user=sc.nextLine();
		
		System.out.println("Password");
		password=sc.nextLine();
		
		new CoreServer();
		String result;
		// List of keys (UUID of the M-Hubs)
    	List<UUID> nodes = new ArrayList<UUID>( mMobileHubs.keySet() );
    	
    	// Asking User for information
    	System.out.println("Maximum temperature to inform Supervisor:");
    	try{
            maxtemp = Double.parseDouble(sc.nextLine());
        }catch(NumberFormatException nfe){
            System.err.println("Invalid Format!");
        }
    	
    	System.out.println("Maximum temperature to inform Fire Department:");
    	try{
            maxtemp_fire = Double.parseDouble(sc.nextLine());
        }catch(NumberFormatException nfe){
            System.err.println("Invalid Format!");
        }
    	
    	System.out.println("Minimum humidity to inform Fire Risk:");
    	try{
            minhumi = Double.parseDouble(sc.nextLine());
        }catch(NumberFormatException nfe){
            System.err.println("Invalid Format!");
        }
		
				        	
        	// Asking Mhubs for information
        	message[0]="[{\n" + 
        			"	\"MEPAQuery\": {\n" + 
        			"		\"type\":\"start\",\n" + 
        			"		\"label\":\"AVGTemp\",\n" + 
        			"		\"rule\":\"SELECT avg(sensorValue[0]) as value_tmp FROM\n" + 
        			"		SensorData(sensorName='Temperature')\n" + 
        			"		.win:time_batch(10 sec)\",\n" + 
        			"		\"target\":\"local\"\n" + 
        			"	}}]";
        	message[1]="[{\n" + 
        			"	\"MEPAQuery\": {\n" + 
        			"		\"type\":\"start\",\n" + 
        			"		\"label\":\"MaxAVG\",\n" + 
        			"		\"rule\":\"SELECT max(value_tmp) as average_tmp FROM\n" + 
        			"		AVGTemp.win:length_batch(3)\",\n" + 
        			"		\"target\":\"global\"\n" + 
        			"	}}]";
        	message[2]="[{\n" + 
        			"	\"MEPAQuery\": {\n" + 
        			"		\"type\":\"start\",\n" + 
        			"		\"label\":\"AVGHumidity\",\n" + 
        			"		\"rule\":\"SELECT avg(sensorValue[0]) as value_humi FROM\n" + 
        			"		SensorData(sensorName='Humidity')\n" + 
        			"		.win:time_batch(10 sec)\",\n" + 
        			"		\"target\":\"global\"\n" + 
        			"	}}]";
        	
        	// Send the message
        	ApplicationMessage appMsg = new ApplicationMessage();
        	appMsg.setPayloadType( PayloadSerialization.JSON );
        	
        	for(int i=0;i<3;i++) {
        		
		        appMsg.setContentObject(message[i]);
		        sendBroadcastMSG(appMsg);
		    //sendUnicastMSG( appMsg, nodeDest );
        	}
			// Destination options to select
        	System.out.println( "\nA broadcast message was sent to all nodes in the network. Please, if you want to see the connected nodes enter r, else enter q:" );
        	
        	do {   
        	// List of keys (UUID of the M-Hubs)
            nodes = new ArrayList<UUID>( mMobileHubs.keySet() );
        	
        	for( int i = 0; i < nodes.size(); ++i ) 
        		System.out.println( i + ": " + nodes.get( i ) );
      
        	System.out.println( "r: refresh" );
        	System.out.println( "q: quit" );
        	
        	
        	result = sc.nextLine();
        	if( result.equals( "r" ) )
        		continue;
        	else if( result.equals( "q" ) ) {
        		System.out.printf("Events (humi+fire): %d \n Humidity sensor data: %d \n Temperature sensor data: %d",events,sensor_humi, sensor_temp);
        		break;
        		}
        	
        	else
        		System.out.println( "Input doesn't match specifications. Try again." );
        	
		    
		} while( true );
		
		 if( sc != null )
	        sc.close();
	}
	
	/**
     * Constructor
     */
    private CoreServer() {
    	// Create a layer and participant
        core = UniversalDDSLayerFactory.getInstance( SupportedDDSVendors.OpenSplice );
        core.createParticipant( UniversalDDSLayerFactory.CNET_DOMAIN );
        // Receive and write topics to domain
        core.createPublisher();
        core.createSubscriber();
        // ClientLib Events
        receiveMessageTopic = core.createTopic( Message.class, Message.class.getSimpleName() );
        core.createDataReader( this, receiveMessageTopic );
        // To ClientLib
        toMobileNodeTopic = core.createTopic( PrivateMessage.class, PrivateMessage.class.getSimpleName() );
        core.createDataWriter( toMobileNodeTopic );
    }
    
    /**
     * Sends a message to all the components (BROADCAST)
     * @param appMSG The application message (e.g. a String message)
     */
    public static void sendBroadcastMSG( ApplicationMessage appMSG ) {
		PrivateMessage privateMSG = new PrivateMessage();
		privateMSG.setGatewayId( UniversalDDSLayerFactory.BROADCAST_ID );
		privateMSG.setNodeId( UniversalDDSLayerFactory.BROADCAST_ID );
		privateMSG.setMessage( Serialization.toProtocolMessage( appMSG ) );
		
		sendCoreMSG( privateMSG );
    }
    
    /**
     * Sends a message to a unique component (UNICAST)
     * @param appMSG The application message (e.g. a String message)
     * @param nodeID The UUID of the receiver
     */
    public static void sendUnicastMSG( ApplicationMessage appMSG, UUID nodeID ) {
		PrivateMessage privateMSG = new PrivateMessage();
		privateMSG.setGatewayId( UniversalDDSLayerFactory.BROADCAST_ID );
		privateMSG.setNodeId( nodeID );
		privateMSG.setMessage( Serialization.toProtocolMessage( appMSG ) );
		
		sendCoreMSG( privateMSG );
    }
    
    /**
     * Writes the message (send)
     * @param privateMSG The message
     */
    private static void sendCoreMSG( PrivateMessage privateMSG ) {
        core.writeTopic( PrivateMessage.class.getSimpleName(), privateMSG );
    }
    
    /**
     * Handle different events identified by a label, verifying whether it occurs inside a given shape file (e.g. inside park territory)
     * @param label The identifier of the event
     * @param object The JSONobject content of the event 
     * @param latitude
     * @param longitude
     * @param timestamp Event time
     * @throws ParseException 
     * @throws SQLException 
     */
    private void handleEvent( final String label, JSONObject object, Double latitude , Double longitude, Timestamp timestamp) throws ParseException, SQLException {

    	ApplicationMessage appMsg = new ApplicationMessage();
    	Statement st = conn.createStatement();
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //For SQL format
    	//System.out.println(sdf.format(timestamp));
    
    	shape_collection local = new shape_collection();
    	Point ponto= local.area(latitude,longitude);
    	events++;
    	if ( !ponto.isEmpty()) {
    		
	    	switch( label ) {
	    		case "MaxAVG":
	    			Double avg = (Double) object.get( "average_tmp" );
	    			
	    			if( avg > maxtemp ) {
	    				
	    			/* A temperatura média em um incêndio depende de vários fatores, como velocidade de propagação
	    			 * tipo de combustível e época da queima.Contudo, a maioria das pesquisas indicam temperaturas máximas
	    			 * entre 600 e 800ºC, embora às vezes eles sejam inferiores a 300ºC ou superiores a 1000ºC 
	    			 * (Artigo: "Comportamento do fogo" do site <www.floresta.ufpr.br>. Acesso em 31.out.2017)*/
	    				
	    				System.out.println( "\n=========================================================" );
	    	        	//events++;
	    				System.out.printf( "Fire detected: %.2fºC %f %f\n", avg, latitude, longitude);
	    				System.out.println( "=========================================================\n" );
	    				appMsg.setContentObject("Fire detected!");
	    				sendUnicastMSG( appMsg, nodeSupervisor );
							
	    				if(avg > maxtemp_fire) 
	    					sendUnicastMSG( appMsg, nodeFire );
	    					
	    				try {
							ResultSet result = st.executeQuery("SELECT * FROM events WHERE status='Fire' AND time_stamp='"+sdf.format(timestamp)+"';");
							
							if (!result.next()) {
								try {
									st.executeUpdate("INSERT INTO events values('Fire',"+latitude+","+longitude+",'"+sdf.format(timestamp)+"',"+avg+");");
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} 
	    			}
	    			/*else if( avg >= 20 && avg <= 30 )
	    				System.out.println( "The weather is perfect!" );
	    			else
	    				System.out.println( "It's too cold!" );*/
	    		break;
	    		case "AVGHumidity":
	    			Double avg_humi= (Double) object.get( "value_humi" );
	    			if(avg_humi<minhumi) {
	    				System.out.println( "\n=========================================================" );
	    	        	//events++;
	    				System.out.printf("Fire risk  %.2f%% humididty %f %f\n", avg_humi, latitude, longitude);
						System.out.println( "=========================================================\n" );
	    				appMsg.setContentObject("Fire risk");
	    				sendUnicastMSG( appMsg, nodeSupervisor );
	    				try {
							ResultSet result = st.executeQuery("SELECT * FROM events WHERE status='Fire risk' AND time_stamp='"+sdf.format(timestamp)+"';");
							
							if (!result.next()) {
								try {
										st.executeUpdate("INSERT INTO events values('Fire risk',"+latitude+","+longitude+",'"+sdf.format(timestamp)+"',"+avg_humi+");");
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} 
					}
	    			
	    		break;    			
	    		default:
	    			break;
	    	
	    	}
	    	
    	}
    }
       
    /**
     * Handle messages (e.g. error or reply)
     * @param object The JSONObject that contains the information
     * @throws ParseException 
     */
    private void handleMessage( final String tag, final JSONObject object ) throws ParseException {
    	final String component = (String) object.get( "component" );
		final String message   = (String) object.get( "message" );
		System.out.println( "\n>>" + tag + "(" + component + "): " + message + "\n" );
    }

	@SuppressWarnings("resource")
	@Override
	public void onNewData( ApplicationObject topicSample ) {
		Message msg = null;
		String tag1 = "Fire Department Listening ...";
		String tag2 = "Supervisor Listening ...";
		Statement st,st1,st2;
		
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //For SQL format
		 //try(FileWriter arq_events= new FileWriter("events.txt",true); FileWriter arq_sensors = new FileWriter("sensor.txt",true)){
		
		
		
		if( topicSample instanceof Message ) {
			msg = (Message) topicSample;
			UUID nodeId = msg.getSenderId();
			UUID gatewayId = msg.getGatewayId();
			
			String content = new String( msg.getContent() );
			if(content.contains(tag1)|| content.contains(tag2)) {
				if (content.contains(tag1))
					nodeFire=nodeId;
					
				else 
					nodeSupervisor=nodeId;
			
				System.out.print( ">> Client connected\n");
				return;	
			}
			
			else if( !mMobileHubs.containsKey( nodeId ) ){  //Mhub first connection
				mMobileHubs.put( nodeId, gatewayId );
				System.out.println( ">> Mobile Hub connected: "+ nodeId);
				ApplicationMessage appMsg = new ApplicationMessage();
	        	appMsg.setPayloadType( PayloadSerialization.JSON );
	        	
	        	for(int i=0;i<3;i++) {
	        		
			        appMsg.setContentObject(message[i]);
			        sendUnicastMSG( appMsg, nodeId ); //Request Event Data
	        	}
				Timestamp timestamp = new Timestamp(System.currentTimeMillis()); 
						try {
							st1 = conn.createStatement();
							ResultSet result = st1.executeQuery("SELECT * FROM mhub WHERE uuid='"+nodeId+"';");
							
							if (!result.next()) {
								try {
								st = conn.createStatement();
								st.execute("INSERT INTO mhub values('"+nodeId+"','"+sdf.format(timestamp)+"');");
																														
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} 
					
				}
			
			
			JSONParser parser = new JSONParser();
			
			try {
	        	JSONObject object = (JSONObject) parser.parse( content );
	        	String tag = (String) object.get( "tag" );
	        	String action = (String) object.get( "action" );
	        	String sensor_name = (String) object.get("sensor_name");
	        	String source = (String) object.get("source");
	        	Long timelong = (Long) object.get( "timestamp" );
	        	Timestamp time = new Timestamp(timelong);  
	        	JSONArray temp = (JSONArray) object.get("sensor_value");	  
	        	        	
	        	switch( tag ) {
	        		case "SensorData":
	        			if(!action.contains("disconnected")) {
	        				if(!sensors.contains(source)) { //add sensor UUID to the database
	        					sensors.add(source);
	        					try {
        							st2 = conn.createStatement();
        							ResultSet result = st2.executeQuery("SELECT * FROM sensor WHERE uuid='"+source+"';");
        							if (!result.next()) {
        								st2 = conn.createStatement();
        								st2.executeUpdate("INSERT INTO sensor values('"+source+"','"+sdf.format(time)+"');");
        							}
        						} catch (SQLException e) {
        							// TODO Auto-generated catch block
        							e.printStackTrace();
        						}
	        					
	        				}
	        				
	        				if (sensor_name.contains("Temperature")) {
	        					sensor_temp++;
	        					//System.out.printf("New data from sensors %s %s\n",tag,object);
	        					if (sensor_temp % 1000 == 0) //each 1000 packages, one is stored in the database
	        						try {
	        							st = conn.createStatement();
	        							st.executeUpdate("INSERT INTO sensor_data values('Temperature',"+temp.get(0)+",'"+sdf.format(time)+"');");

	        						} catch (SQLException e) {
	        							// TODO Auto-generated catch block
	        							e.printStackTrace();
	        						}
	        				}
	        				else if (sensor_name.contains("Humidity")){
	        					sensor_humi++;
		        				if (sensor_humi % 1000 == 0) //each 1000 packages, one is stored in the database
		        					try {
		        						st = conn.createStatement();
		        						st.executeUpdate("INSERT INTO sensor_data values('Humidity',"+temp.get(0)+",'"+sdf.format(time)+"');");
	
		        					} catch (SQLException e) {
		        						// TODO Auto-generated catch block
		        						e.printStackTrace();
		        					}
	        				}}
	        		break;
	        		
	        		case "EventData":
	        			final String label = (String) object.get( "label" );
	        			JSONObject data  = (JSONObject) object.get( "data" );
	        			Double latitude = (Double) object.get("latitude");
	        			Double longitude = (Double) object.get("longitude");
	        			handleEvent( label, data , latitude, longitude, time);
		        	break;
		        	
	        		case "ReplyData":		        	
	        		case "ErrorData":
	        			handleMessage( tag, object );
			        break;
	        	}
			} catch( Exception ex ) {
				System.out.println( ex.getMessage() );
			}
		}
		
	}
	/* Deprecated
	public static Boolean camping_area(Double latitude, Double longitude) {
		Runner r = new Runner();
		
		return r.coordinate_is_inside_polygon(latitude, longitude);
	}
	*/
	/**
	 * A simple check to see if a string is a valid number 
	 * 
	 * @param s The number to be checked.
	 * @return true  It is a number.
	 *         false It is not a number.
	 */

	public static Boolean isNumber( String s ) {
		try {
            Integer.parseInt( s );
        }
		catch( NumberFormatException e ) {
			return false;			
		}
		return true;
	}
}
