/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.PrepStmt;
import com.triniforce.db.dml.ResSet;
import com.triniforce.db.dml.StmtContainer;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.dbo.DBOTableDef;
import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.soap.MultiClassServiceInvoker;

public class BasicServerInvokerTest extends BasicServerTestCase {
	
	public static class TestTab extends DBOTableDef{
		public TestTab() {
			setDbName("TEST_TAB_FOR_BASICSERV");
			addField(1, FieldDef.createScalarField("f1", ColumnType.INT, false));
		}
	} 
	
	@Override
	protected void setUp() throws Exception {
		addPlugin(new PKPlugin() {
			
			@Override
			public void doRegistration() {
				
			}
			
			@Override
			public void doExtensionPointsRegistration() {
				putExtension(PKEPDBObjects.class, TestTab.class);
			}
		});
		
		super.setUp();
	}
	
	public class TestSvc{
		public void addrec(int v){
			StmtContainer sc = SrvApiAlgs2.getStmtContainer();
			PrepStmt ps = sc.prepareStatement("insert into TEST_TAB_FOR_BASICSERV (f1) values (?)");
			ps.setObject(1, v);
			ps.execute();
			sc.close();
		}
		
		public int readrec(){
			StmtContainer sc = SrvApiAlgs2.getStmtContainer();
			try{
				PrepStmt ps = sc.prepareStatement("select f1 from  TEST_TAB_FOR_BASICSERV");
				ResSet rs = ps.executeQuery();
				return rs.next() ? rs.getInt(1) : -1;
			}finally{
				sc.close();
			}
			
		}
	}

	public void testInvokeService() {
		MultiClassServiceInvoker svc = new MultiClassServiceInvoker();
		svc.addServiceEndpoint("test", new TestSvc());
		
		BasicServerInvoker invoker = new BasicServerInvoker(getServer(), svc);
		
		invoker.invokeService("test_addrec", 123);
		assertEquals(123, invoker.invokeService("test_readrec"));
		
	}

}
