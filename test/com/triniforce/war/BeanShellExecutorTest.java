/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.utils.ApiStack;
import com.triniforce.war.api.IBasicServerConfig;

public class BeanShellExecutorTest extends BasicServerRunningTestCase {
	
	private Mockery ctx;
	private IBasicServerConfig cfg;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ctx = new Mockery();
		cfg = ctx.mock(IBasicServerConfig.class);
		ApiStack.pushInterface(IBasicServerConfig.class, cfg);
	}
	
	@Override
	protected void tearDown() throws Exception {
		ApiStack.popInterface(1);
		super.tearDown();
	}

	@Override
	public void test() throws Exception {
		setPass("PW0RD");
		BeanShellExecutor executor = new BeanShellExecutor();
		
		String res = executor.execBeanShell("PW0RD", "System.out.println(\"Hello !\")");
		assertEquals("Hello !\r\n", res);
		
		try{
			executor.execBeanShell("PW0RD1", "System.out.print(\"Hello !\")");
			fail();
		}catch(EAuthException e){}
		
		assertEquals("HI test\r\n", executor.execBeanShell("PW0RD", "println(\"HI test\")"));
		
		assertTrue(executor.execBeanShell("PW0RD", "println(diag.getEnvironment())").contains("Servlet Context"));
		
		setPass("");
		try{
			executor.execBeanShell("", "System.out.print(\"Hello !\")");
			fail();
		}catch(EAuthException e){}
		
		

	}

	private void setPass(final String pwd) {
		ctx.checking(new Expectations(){{
			Properties props = new Properties();
			props.put(IBasicServerConfig.SCRIPT_PWD_KEY, pwd);
			allowing(cfg).getProperties(); will(returnValue(props));
		}});
		
	}
}
