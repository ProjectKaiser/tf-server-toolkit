/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.plugins.kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.dbo.DBOTableDef;
import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.server.plugins.kernel.ep.api.IFinitApi;
import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIs;
import com.triniforce.server.plugins.kernel.ep.br.PKEPBackupRestore;
import com.triniforce.server.plugins.kernel.ep.sp.PKEPServerProcedures;
import com.triniforce.server.plugins.kernel.ep.sp.ServerProcedure;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class BasicServerTest extends BasicServerTestCase {
	
	
	static boolean bFINITED = false;
	public static class TestPKEPAPI implements IPKEPAPI, IFinitApi{

		@Override
		public Class getImplementedInterface() {
			return TestPKEPAPI.class;
		}

		@Override
		public void finitApi() {
			bFINITED = true;
			
		}
		
	}

    static TimeZone tz1 = TimeZone.getTimeZone("Europe/Moscow");
    
	public static class Handlers{
		public IHandler handler1;
		public IHandler handler2;
	}
	
	public static interface IHandler{
		public int execute(int a, int b);
	}
	
	public void testInitFinit(){
		getServer().getExtensionPoint(PKEPBackupRestore.class);
		getServer().finit();
		getServer().getExtensionPoint(PKEPBackupRestore.class);
		getServer().init();
	}
	
	public static class TestExtTab  extends DBOTableDef{
		public TestExtTab() {
			super(TestExtTab.class.getName());
			setDbName("TEST_EXT_TAB");
			addField(1, FieldDef.createScalarField("id", ColumnType.INT, true));
			setExternalTable(true);
		}
	}
	
	static class TestUProc extends DPPProcPlugin{
		
		@Override
		public void doRegistration(ISORegistration reg)
				throws EDBObjectException {
			super.doRegistration(reg);
			doExtensionPointsRegistration();
		}
		
		@Override
		public void doExtensionPointsRegistration() {
			putExtension(PKEPDBObjects.class, TestExtTab.class);
			
			putExtension(PKEPAPIs.class, TestPKEPAPI.class);
		}

		@Override
		public void doRegistration() {
		}
		
	}  
	
	@Override
	public void test() throws Exception {
		{
			System.setProperty(BasicServer.TZ_PROPERTY, "Europe/Moscow");
		    @SuppressWarnings("unused")
	        BasicServer bs = new BasicServer();
            assertEquals(tz1.getID(), System.getProperty(BasicServer.TZ_PROPERTY));
		    TimeZone def = TimeZone.getDefault();
		    TimeZone gmt = TimeZone.getTimeZone("GMT");
		    assertEquals(gmt, def);
	        m_server.enterMode(Mode.Running);
	        try{
	            TimeZone tz = ApiStack.getInterface(TimeZone.class);
	            assertEquals("Europe/Helsinki", tz.getID());
	            
	            ApiAlgs.getLog(this).trace(new Date());
	            
	        } finally{
	            m_server.leaveMode();
	        }
		}
		{
			m_coreApi.setIntfImplementor(TimeZone.class, TimeZone.getTimeZone("GMT+05:00"));
			
			BasicServer bs = new BasicServer(m_coreApi);
			bs.doRegistration();
			if(bs.isDbModificationNeeded())
				bs.doDbModification();
			bs.enterMode(Mode.Running);
			try{
				assertEquals("GMT+05:00", ApiStack.getInterface(TimeZone.class).getID());
			}finally{
				bs.leaveMode();
			}
			
		    
		}
	 
	}
	
	@Override
	protected void setUp() throws Exception {
		addPlugin(new TestUProc());
		super.setUp();
	}
	
	public static class MyProcedure extends ServerProcedure{
		boolean m_requireRunningMode = false;
		@Override
		public Object invoke(Object... arguments) {
		    ApiStack.getInterface(IPKExtensionPoint.class);
			ISrvSmartTran st = ApiStack.queryInterface(ISrvSmartTran.class);
			return null != st;
		}

		@Override
		public boolean requireRunningMode() {
			return m_requireRunningMode;
		}
		
	}
	
	public void testDescriptions(){
		testExtensions();
	}

	public void testExtensions(){
		checkExtensions(this, getServer());
	}
	
	public void testProcedures(){
		IPKExtensionPoint ep = m_server.getExtensionPoint(PKEPServerProcedures.class.getName());
		assertEquals(  BasicServerCorePlugin.class.getName(), ep.getPluginId());
		
		//test invoke procedure
		{
			MyProcedure mp = new MyProcedure();
			getServer().getExtensionPoint(PKEPServerProcedures.class).putExtension(mp);
			PKEPServerProcedures sp = (PKEPServerProcedures) getServer().getExtensionPoint(PKEPServerProcedures.class);
			assertEquals(false, sp.invokeProcedure(false, MyProcedure.class.getName()));
			assertEquals(true, sp.invokeProcedure(true, MyProcedure.class.getName()));
			assertEquals(false, sp.invokeProcedure(MyProcedure.class.getName()));
			
			//default procedure setting requireRunningMode
			assertEquals(false, sp.invokeProcedure(MyProcedure.class.getName()));
			mp.m_requireRunningMode = true;
			assertEquals(true, sp.invokeProcedure(MyProcedure.class.getName()));
		}
		
	}
	
	public void testPlayBeanShell() throws Throwable {
		
	    {//do not enter running mode
            File script = File.createTempFile("autoexec", ".js");
            {
                script.deleteOnExit();
                FileWriter outFile = new FileWriter(script);
                PrintWriter pw = new PrintWriter(outFile);
                
                
                pw.println("import com.triniforce.utils.*;");
                pw.println("com.triniforce.server.srvapi.IBasicServer bs = ApiStack.getInterface(com.triniforce.server.srvapi.IBasicServer.class);");
                pw.println("ApiAlgs.getLog(bs).trace(ApiStack.getApi().toString());");
                pw.close();
                m_server.executeBeanShell(script, false);
            }	        
	        
	    }
	    
		{//does not exist execution
			File script = File.createTempFile("autoexec", ".js");
			script.delete();

			m_server.executeBeanShell(script);
		}
		{//file with error
			
			File script = File.createTempFile("autoexec", ".js");
			script.deleteOnExit();
			FileWriter outFile = new FileWriter(script);
			PrintWriter pw = new PrintWriter(outFile);
			pw.print("i = 10;");
			pw.close();
			incExpectedLogErrorCount(1);
			m_server.executeBeanShell(script);
		}
		{//good script
			
			File script = File.createTempFile("autoexec", ".js");
			{
				script.deleteOnExit();
				FileWriter outFile = new FileWriter(script);
				PrintWriter pw = new PrintWriter(outFile);
				pw.println("import com.triniforce.server.plugins.kernel.BasicServerTest.Handlers;");
				pw.println("import com.triniforce.server.plugins.kernel.BasicServerTest.IHandler;");
				pw.println("import com.triniforce.utils.*;");
				pw.println("Handlers hh = ApiStack.getInterface(Handlers.class);");
				pw.println("hh.handler1 = new IHandler(){public int execute(int a, int b){return a + b;}};");
				pw.println("hh.handler2 = new IHandler(){public int execute(int a, int b){return a - b;}};");
				pw.close();
			}
			
			Handlers hh = new Handlers();
			
			Api api = new Api();
			api.setIntfImplementor(Handlers.class, hh);
			ApiStack.pushApi(api);
			try{
				m_server.executeBeanShell(script);	
			}finally{
				ApiStack.popApi();
			}
			assertEquals( 3, hh.handler1.execute(1,2));
			assertEquals( -1, hh.handler2.execute(1,2));
			
		}
		
	}
	
	@Override
	protected void setCoreApiInteraces(Api api) {
		super.setCoreApiInteraces(api);
		api.setIntfImplementor(IIdDef.class, null);
		TimeZone tz = TimeZone.getTimeZone("Europe/Helsinki");
		api.setIntfImplementor(TimeZone.class, tz);
	}
	
	public void testInterfaces(){
		trace(Arrays.asList(TimeZone.getAvailableIDs()));
		m_server.enterMode(Mode.Running);
		try{
		    ApiStack.getInterface(ITaskExecutors.class);		    
			assertEquals(ColumnType.LONG, IIdDef.Helper.getFieldDef().getType());
		} finally{
			m_server.leaveMode();
		}
	}
	
	public void testGetCompletedProcedures(){
		m_server.enterMode(Mode.Upgrade);
		try{
			ISrvSmartTranFactory trf = ApiStack.getInterface(ISrvSmartTranFactory.class);
			trf.push();
			try{
				ISODbInfo dbInfo = ApiStack.getInterface(ISODbInfo.class);
				Set<String> list = dbInfo.getCompletedUpgradeProcedures();
				assertNotNull(list);
				assertFalse(list.contains("unknownFun"));
				
				list = dbInfo.getCompletedDataPreparationProcedures();
				assertNotNull(list);
				assertFalse(list.contains("unknownFun"));
//				assertTrue(list.toString(), list.contains(TestUProc.class.getName()));
			} finally{
				trf.pop();
			}
		}finally{
			m_server.leaveMode();
		}
	}
	
	public void testPluginRegistration(){
		BasicServer bs = getServer();
        assertNotNull(bs.getEntity(TestExtTab.class.getName()));
        assertEquals("TEST_EXT_TAB", bs.getTableDbName(TestExtTab.class.getName()));
	}
	
	public void testFinitAfterShutdown() throws SQLException{
		getPool().m_ds.close();
		getServer().stopAndFinit();
		assertTrue(bFINITED);
		
		m_pool = null;//reopen
		DATA_SOURCE = null;
		getPool();
		
	}


}
