package br.pucrio.inf.lac;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines the database connection to the SFS (Smart Forest Server). We use a PostgreSQL server. 
 * @author Guilherme Neumann
 *  */

public class database_connection {

	/**
	 * It connects to PostgreSQL
	 * @param ip string 
	 * @param port string
	 * @param db database name 
	 * @param user
	 * @param password
	 * @return sql connection
	 */
	public Connection database_connection(String ip, String port, String db, String user, String password) {
		try 
		{ 
			Class.forName("org.postgresql.Driver"); 
			String url = "jdbc:postgresql://"+ip+":"+port+"/"+db; 
			Properties props = new Properties(); 
			props.setProperty("user",user); 
			props.setProperty("password",password); 
			Connection conn = DriverManager.getConnection(url, props); 
			System.out.println("Database server connected");
			System.out.println(conn);
			return conn;
		} 
		catch (Exception e) 
		{ 
			System.out.println("Problem connecting to database"); 
			Logger.getLogger(database_connection.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		 
	}

}
