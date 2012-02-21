/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.plugins.kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.triniforce.db.test.BasicServerTestCase;
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
	
	public void testPlayBeanShell() throws Throwable {
		
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
}
