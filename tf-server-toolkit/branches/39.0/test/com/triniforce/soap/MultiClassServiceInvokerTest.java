/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.MultiClassServiceInvoker.EServiceEndpointAlreadyRegistered;
import com.triniforce.soap.MultiClassServiceInvoker.EServiceEnpointNotFound;
import com.triniforce.soap.RequestHandler.IServiceInvoker;

public class MultiClassServiceInvokerTest extends TFTestCase {
	
	static class TestSrv{
		static String v = null; 
		public void fun1(String s){
			v = s;
		}
		
		public String fun2(){
			return "result";
		}
	}
	
	static class TestSrv2{
		public String fun2(String v){
			return v;
		}
	}

	public void testAddServiceEndpoint() {
		MultiClassServiceInvoker si = new MultiClassServiceInvoker();
		TestSrv srv1 = new TestSrv();
		si.addServiceEndpoint("tst", srv1);
		si.addServiceEndpoint("tst2", srv1);
		
		try{
			si.addServiceEndpoint("tst", srv1);
			fail();
		} catch(EServiceEndpointAlreadyRegistered e){
			assertEquals("tst", e.getMessage());
		}
		
		try{
			si.addServiceEndpoint("tst_ff", srv1);
			fail();
		} catch(IllegalArgumentException e){
			assertTrue(e.getMessage().contains("tst_ff"));
		}
		
		Mockery ctx = new Mockery();
		final IServiceInvoker innerSI = ctx.mock(IServiceInvoker.class);
		si.addServiceEndpoint("secondary", innerSI);
		
		ctx.checking(new Expectations(){
			{
				one(innerSI).invokeService("function_012", 5,6,7); will(returnValue(10001));
			}
		});
		
		assertEquals(10001, si.invokeService("secondary_function_012", 5,6,7));
	}

	public void testInvokeService() {
		MultiClassServiceInvoker si = new MultiClassServiceInvoker();
		TestSrv srv1 = new TestSrv();
		si.addServiceEndpoint("tst", srv1);
		
		si.invokeService("tst_fun1", "argument");
		
		assertEquals("argument", TestSrv.v);
		
		assertEquals("result", si.invokeService("tst_fun2"));
		
		TestSrv2 srv2 = new TestSrv2();
		si.addServiceEndpoint("tstep2", srv2);
		
		assertEquals("return_me", si.invokeService("tstep2_fun2", "return_me"));
		
		try{
			si.invokeService("unk_method");
			fail();
		}catch(EServiceEnpointNotFound e){}
		
	}
	

}
