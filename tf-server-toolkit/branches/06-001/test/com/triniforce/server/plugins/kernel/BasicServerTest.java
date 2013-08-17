/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.plugins.kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Set;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.server.plugins.kernel.ep.sp.PKEPServerProcedures;
import com.triniforce.server.plugins.kernel.ep.sp.ServerProcedure;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class BasicServerTest extends BasicServerTestCase {

	public static class Handlers{
		public IHandler handler1;
		public IHandler handler2;
	}
	
	public static interface IHandler{
		public int execute(int a, int b);
	}
	
	static class TestUProc extends DPPProcPlugin{}  
	
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
		checkExtensions(getServer());
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
	}
	
	public void testDefaultInterfaces(){
		m_server.enterMode(Mode.Running);
		try{
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
				assertTrue(list.toString(), list.contains(TestUProc.class.getName()));
			} finally{
				trf.pop();
			}
		}finally{
			m_server.leaveMode();
		}
	}
	
}