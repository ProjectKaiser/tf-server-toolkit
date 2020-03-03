/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.services;

import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiStack;

public class PKEPServicesTest extends ServicesTestCase {
	
	public static Object SYNCH_OBJ1 = new Object();
	static volatile boolean  bComplete = false;
	
	public static class TestService extends Service{
		@Override
		public void doCycle() throws Throwable {
			super.doCycle();
			synchronized (SYNCH_OBJ1) {
				SYNCH_OBJ1.notify();
			}
			bComplete = true;
		}
	}

	private PKEPServices ep;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IBasicServer rep = ApiStack.getInterface(IBasicServer.class);
		ep = (PKEPServices) rep.getExtensionPoint(PKEPServices.class);
		ep.putExtension(TestService.class);
	}
	
	@Override
	protected void tearDown() throws Exception {
        IBasicServer rep = ApiStack.getInterface(IBasicServer.class);
		ep = (PKEPServices) rep.getExtensionPoint(PKEPServices.class);
		ep.removeExtension(TestService.class.getName());
		super.tearDown();
	}

	public void testStartAll() throws InterruptedException{
		IBasicServer server = ApiStack.getInterface(IBasicServer.class);

		long t0 = System.currentTimeMillis();
		synchronized(SYNCH_OBJ1){
			server.startServices();
			SYNCH_OBJ1.wait(5000L);
		}
		try{
			trace("Exec time: " + (System.currentTimeMillis() - t0) + "ms");
			assertTrue(bComplete);
			
		}finally{
			server.stopServicesAndWait();
		}
		assertEquals(IService.State.STOPPED, getService().getState());
	}

	private IService getService() {
		return ep.getExtension(TestService.class).getInstance();
		
	}
	
}
