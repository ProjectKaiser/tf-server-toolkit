/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.services;

import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
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
		getServer().enterMode(Mode.Running);
		IBasicServer rep = ApiStack.getInterface(IBasicServer.class);
		ep = (PKEPServices) rep.getExtensionPoint(PKEPServices.class);
		ep.putExtension(TestService.class);
		
        IThrdWatcherRegistrator twr = ApiStack.getInterface(IThrdWatcherRegistrator.class);
        twr.registerThread(Thread.currentThread(), null);

	}
	
	@Override
	protected void tearDown() throws Exception {
        IThrdWatcherRegistrator twr = ApiStack.getInterface(IThrdWatcherRegistrator.class);
        twr.unregisterThread(Thread.currentThread());

        IBasicServer rep = ApiStack.getInterface(IBasicServer.class);
		ep = (PKEPServices) rep.getExtensionPoint(PKEPServices.class);
		ep.removeExtension(TestService.class.getName());
        
		getServer().leaveMode();
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
			server.stopServices();
		}
		Thread.sleep(2000L);
		assertEquals(IService.State.STOPPED, getService().getState());
	}

	private IService getService() {
		return ep.getExtension(TestService.class).getInstance();
		
	}
	
}
