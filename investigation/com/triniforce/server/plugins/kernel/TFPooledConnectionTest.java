/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.dbcp2.BasicDataSource;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.TFPooledConnection.IStackTraceInfo;
import com.triniforce.server.srvapi.IPooledConnection.StackTraceRec;
import com.triniforce.utils.ApiStack;

public class TFPooledConnectionTest extends TFTestCase {

	
	public void test() throws Exception {
		TFPooledConnection pool = new TFPooledConnection(getDataSource(), 10);
		
		for(int i=0; i< 12; i++){
			pool.getPooledConnection();
		}
	}
	
	static class TestT extends Thread{
		
		private TFPooledConnection m_pool;
		public TestT(TFPooledConnection pool) {
			m_pool = pool;
		}
		@Override
		public void run() {
			Connection c;
			try {
				c = m_pool.getPooledConnection();
				m_pool.returnConnection(c);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	public void testThreadSafe() throws InterruptedException, SQLException{
		TFPooledConnection pool = new TFPooledConnection(getDataSource(), 10);
		Connection con1 = pool.getPooledConnection();
		TestT t1 = new TestT(pool);
		TestT t2 = new TestT(pool);
		
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		
		con1.close();
	}
	
	boolean DOTHROW = false;
	public void testGetConnection() throws SQLException {
		BasicDataSource ds = new BasicDataSource() {
			public Connection getConnection() throws SQLException {
				if(DOTHROW)
					throw new SQLException("testthrow");
				return getDataSource().getConnection();				
			};
		};
		TFPooledConnection pool = new TFPooledConnection(ds, 10);
		ApiStack.pushInterface(IStackTraceInfo.class, new IStackTraceInfo() {

			@Override
			public String getInfo() {
				return "TEST_INFO0044";
			}
			
		});
		try {
			Connection c = pool.getPooledConnection();
			try{
				Collection<StackTraceRec> res = pool.getTakenConnectionPoints();
				assertEquals(1, res.size());
				StackTraceRec v0 = res.iterator().next();
				assertEquals("TEST_INFO0044", v0.getInfo());
			}finally {
				pool.returnConnection(c);
			}
		}finally {
			ApiStack.popInterface(1);
		}
			
	}
}
