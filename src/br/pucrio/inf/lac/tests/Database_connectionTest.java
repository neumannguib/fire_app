/**
 * 
 */
package br.pucrio.inf.lac.tests;

import static org.junit.Assert.*;

import java.sql.Connection;

import org.junit.*;

import br.pucrio.inf.lac.database_connection;

/**
 * @author Guilherme Neumann
 *
 */
public class Database_connectionTest extends database_connection {

	/**
	 * Test method for {@link br.pucrio.inf.lac.database_connection#database_connection(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testDatabase_connection() {
		Connection conn =null;
		conn = new database_connection().database_connection("localhost","5432","Fire_db","SFS","adminadmin"); 
		assertNotNull(conn);
	}

}
