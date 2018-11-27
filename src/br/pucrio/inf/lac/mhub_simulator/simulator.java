package br.pucrio.inf.lac.mhub_simulator;




public class simulator {
	private static String		gatewayIP   = "127.0.0.1";
	private static int			gatewayPort = 5500;
	
	public static void main(String[] args) {
		mhub nodes[];
		
		int number=20;
	    int num_sensors =10;
	    int num_events=10;
	    if(args.length!=0) {
	    	gatewayIP=args[0];
	    	gatewayPort= Integer.parseInt(args[1]);
	    	number = Integer.parseInt(args[2]);
	    	num_sensors = Integer.parseInt(args[3]);
	    	num_events= Integer.parseInt(args[4]);
	    }
		nodes = new mhub[number];
		long tempoInicio = System.currentTimeMillis();

		for ( int i = 0; i < number; i++ ) 
			nodes[i]=new mhub(gatewayIP,gatewayPort);
			try {
				Thread.sleep(1000); //milliseconds to wait creation
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		for ( int i = 0; i < number; i++ )
			nodes[i].newMessage(num_sensors,num_events);
		
		long tempoFim = System.currentTimeMillis();
		System.out.println("Tempo decorrido em milissegundos: " + (tempoFim -tempoInicio));
	
	}

}
