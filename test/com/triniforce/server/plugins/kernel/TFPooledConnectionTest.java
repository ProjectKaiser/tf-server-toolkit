/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;

import com.triniforce.db.test.TFTestCase;

public class TFPooledConnectionTest extends TFTestCase {

	public void testMaxActiveConnections() throws SQLException{
		closeDataSource();
		TFPooledConnection pool = new TFPooledConnection(getDataSource(), 5);
		Connection actives[] = {
				pool.getPooledConnection(),
				pool.getPooledConnection(),
				pool.getPooledConnection(),
				pool.getPooledConnection(),
				pool.getPooledConnection()
		};
		try{
			try{
				pool.getPooledConnection();
				fail();
			}catch(SQLException e){
			}
		}finally{
			for (Connection connection : actives) {
				pool.returnConnection(connection);
			}
		}
	}
}
