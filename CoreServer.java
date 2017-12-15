package br.pucrio.inf.lac;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.json.simple.JSONObject;
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

public class CoreServer implements UDIDataReaderListener<ApplicationObject> {
	/** DEBUG */
	private static final String TAG = CoreServer.class.getSimpleName();

	/** SDDL Elements */
    private Object receiveMessageTopic;
    private Object toMobileNodeTopic;
    private static SddlLayer core;
    
    /** Mobile Hubs Data */
    private static final Map<UUID, UUID> mMobileHubs = new HashMap<>();
    
    /** Input reader */
    private static Scanner sc = new Scanner( System.in );
    
    private static UUID nodeFire=null; 
    private static UUID nodeSupervisor=null;
    public static long time1=0;
    public static int events=0;
    public static int sensor=0;
    
	public static void main( String[] args ) {
		new CoreServer();
		
		do {
			//UUID nodeDest = null;
			String message[] = null, result;
			message=new String[10];
			// List of keys (UUID of the M-Hubs)
        	List<UUID> nodes = new ArrayList<UUID>( mMobileHubs.keySet() );
        	
        	// Asking Mhubs for information
        	
        	message[0]="[{\n" + 
        			"	\"MEPAQuery\": {\n" + 
        			"		\"type\":\"add\",\n" + 
        			"		\"label\":\"AVGTemp\",\n" + 
        			"		\"rule\":\"SELECT avg(sensorValue[0]) as value_tmp FROM\n" + 
        			"		SensorData(sensorName='Temperature')\n" + 
        			"		.win:time_batch(10 sec)\",\n" + 
        			"		\"target\":\"local\"\n" + 
        			"	}}]";
        	message[1]="[{\n" + 
        			"	\"MEPAQuery\": {\n" + 
        			"		\"type\":\"add\",\n" + 
        			"		\"label\":\"MaxAVG\",\n" + 
        			"		\"rule\":\"SELECT max(value_tmp) as average_tmp FROM\n" + 
        			"		AVGTemp.win:length_batch(3)\",\n" + 
        			"		\"target\":\"global\"\n" + 
        			"	}}]";
        	message[2]="[{\n" + 
        			"	\"MEPAQuery\": {\n" + 
        			"		\"type\":\"add\",\n" + 
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
        	
        	        	
        	
        	for( int i = 0; i < nodes.size(); ++i ) 
        		System.out.println( i + ": " + nodes.get( i ) );
      
        	System.out.println( "r: refresh" );
        	System.out.println( "q: quit" );
        	
        	
        	result = sc.nextLine();
        	if( result.equals( "r" ) )
        		continue;
        	else if( result.equals( "q" ) ) {
        		System.out.printf("Tempo1:%d",time1);
        		System.out.printf("Eventos: %d",events);
        		System.out.printf("SEnsordata: %d",sensor);
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
     * Handle different events identified by a label
     * @param label The identifier of the event
     * @param data The data content of the event in JSON
     * @throws ParseException 
     */
    private void handleEvent( final String label, final String data, Double latitude , Double longitude) throws ParseException {
    	JSONParser parser = new JSONParser();
    	JSONObject object = (JSONObject) parser.parse( data );
    	ApplicationMessage appMsg = new ApplicationMessage();
    	final String local = new String(area(latitude,longitude));
    	System.out.println( "\n===========================" );
    	events++;
    	switch( label ) {
    		case "MaxAVG":
    			Double avg = (Double) object.get( "average_tmp" );
    			
    			if( avg > 100 ) {
    			/* A temperatura média em um incêndio depende de vários fatores, como velocidade de propagação
    			 * tipo de combustível e época da queima.Contudo, a maioria das pesquisas indicam temperaturas máximas
    			 * entre 600 e 800ºC, embora às vezes eles sejam inferiores a 300ºC ou superiores a 1000ºC 
    			 * (Artigo: "Comportamento do fogo" do site <www.floresta.ufpr.br>. Acesso em 31.out.2017)*/
    				System.out.println( "Fire detected!" + local);
    				appMsg.setContentObject("Fire detected!"+ local);
    				sendUnicastMSG( appMsg, nodeSupervisor );
    				if(avg>300 || local.contains("camping_area")) {
    					sendUnicastMSG( appMsg, nodeFire );
    				}
    			}
    			else if( avg >= 20 && avg <= 30 )
    				System.out.println( "The weather is perfect!" );
    			else
    				System.out.println( "It's too cold!" );
    		break;
    		case "AVGHumidity":
    			Double avg_humi= (Double) object.get( "value_humi" );
    			if(avg_humi<35) {
    				System.out.println("Fire risk" + local);
    				appMsg.setContentObject("Fire risk"+ local);
    				sendUnicastMSG( appMsg, nodeSupervisor );
    			}
    			else
    				System.out.println("Normal Humidity"+local);
    		break;    			
    		default:
    			break;
    	
    	}
    	System.out.println( "===========================\n" );
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

	@Override
	public void onNewData( ApplicationObject topicSample ) {
		Message msg = null;
		String tag1 = "Fire Department Listening ...";
		String tag2 = "Supervisor Listening ...";
		try(FileWriter arq_events= new FileWriter("events.txt",true); FileWriter arq_sensors = new FileWriter("sensor.txt",true)){
		
		
		
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
			
				System.out.print( ">>" + TAG + ": Client connected\n");
				return;	
			}
			
			else if( !mMobileHubs.containsKey( nodeId ) ){
				mMobileHubs.put( nodeId, gatewayId );
				System.out.println( ">>" + TAG + ": Mobile Hub connected" );
			}
			
			//String content = new String( msg.getContent() );
			JSONParser parser = new JSONParser();
			
			try {
	        	JSONObject object = (JSONObject) parser.parse( content );
	        	String tag = (String) object.get( "tag" );
	        	String action = (String) object.get( "action" );
	        	Long time = (Long) object.get( "timestamp" );
	        	
	        	
	        	
	        	switch( tag ) {
	        		case "SensorData":
	        			
	        			if(!action.contains("disconnected")) {
	        					sensor++;
	        					System.out.printf("New data from sensors %s %s\n",tag,object);
	        					arq_sensors.write(object.toString());
	        			}
	        			if (time1==0)
	    	        		time1=time;
	        		break;
	        		
	        		case "EventData":
	        			final String label = (String) object.get( "label" );
	        			final String data  = (String) object.get( "data" );
	        			arq_events.write(object.toString());
	        			//System.out.printf("New data from sensors %s %s\n",tag,object);
	        			Double latitude = (Double) object.get("latitude");
	        			Double longitude = (Double) object.get("longitude");
	        			handleEvent( label, data , latitude, longitude);
		        	break;
		        	
	        		case "ReplyData":		        	
	        		case "ErrorData":
	        			handleMessage( tag, object );
			        break;
	        	}
			} catch( Exception ex ) {
				System.out.println( ex.getMessage() );
			}
			arq_events.close();
			arq_sensors.close();
		}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * A simple check to see if a string is a valid number 
	 * 
	 * @param s The number to be checked.
	 * @return true  It is a number.
	 *         false It is not a number.
	 */
	public static String area (Double latitude, Double longitude) {
		shape_collection shape = new shape_collection();
		return shape.area(latitude,longitude).toString();
	}
	public static Boolean camping_area(Double latitude, Double longitude) {
		Runner r = new Runner();
		
		return r.coordinate_is_inside_polygon(latitude, longitude);
	}
	
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
