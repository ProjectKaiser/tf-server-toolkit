/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.TestResult;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.dbo.DBOTableDef;
import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class BasicServerTestCaseTest extends TFTestCase {
	public static class TestExt extends DBOTableDef{
		
		public TestExt() {
			addStringField(1, "f1",ColumnType.CHAR, 10, false, null);
		}
	}
	
	static boolean FINITED = false;
	
	static class TestPl extends PKPlugin{
		@Override
		public void doRegistration() {
			
			putExtension(PKEPDBObjects.class, TestExt.class);
		}

		@Override
		public void doExtensionPointsRegistration() {
		}
		
		@Override
		public void finit() {
			super.finit();
			FINITED = true;
		}
		
	}
	
	public static class Test1 extends BasicServerTestCase{
		public void test1() throws Exception {
			getServer().enterMode(Mode.Running);
			try{
				Statement st = ApiStack.getInterface(Connection.class).createStatement();
				ResultSet rs = st.executeQuery("select * from " + ActualStateBL.ACT_STATE_TABLE);
				assertTrue(rs.next());
				rs.close();
				st.close();
			}finally{
				getServer().leaveMode();
			}
		}
	}

	public void test() throws Exception {
		BasicServerTestCase test = new BasicServerTestCase();
		test.setName("test");
		test.run();
		assertNotNull(test.getServer());
		
		test = new BasicServerTestCase();
		test.setName("test");
		test.addPlugin(new TestPl());
		test.run();
		test.getServer().enterMode(Mode.Running);
		try{
			assertNotNull(test.getServer().getExtension(PKEPDBObjects.class, TestExt.class));
		}finally{
			test.getServer().leaveMode();	
		}
		
		closeDataSource();
		test = new Test1();
		test.setName("test1");
		TestResult res = test.run();
		assertEquals(0, res.failureCount());
		assertEquals(0, res.errorCount());
		
		assertTrue(FINITED);
	};

	public static class RunningServerTestCase extends BasicServerTestCase{
		
		public RunningServerTestCase() {
			super();
			setName("test_FOO");
		}
		@Override
		protected void setUp() throws Exception {
			addPlugin(new TestPl());
			super.setUp();
			getServer().enterMode(Mode.Running);
		}
		
		public void test_FOO() throws Exception {
		}
		
		@Override
		protected void tearDown() throws Exception {
			getServer().leaveMode();
			super.tearDown();
		}
	}
	
	public void testPerf(){
		TestResult res = new TestResult();
		RunningServerTestCase test1 = new RunningServerTestCase();
		RunningServerTestCase test2 = new RunningServerTestCase();
		long tst = System.nanoTime();
		test1.run(res);
		long tend = System.nanoTime();
		trace(String.format("Exec 1 time: %10.3fms", (tend-tst)/1000000.));
		tst = System.nanoTime();
		test2.run(res);
		tend = System.nanoTime();
		trace(String.format("Exec 2 time: %10.3fms", (tend-tst)/1000000.));
		
		assertEquals(0, res.errorCount() + res.failureCount());
	}
}
