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
	
	static boolean bComplete = false;
	
	public static class TestService extends Service{
		@Override
		public void doCycle() throws Throwable {
			super.doCycle();
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
		getServer().leaveMode();
		super.tearDown();
	}

	public void testStartAll() throws InterruptedException{
		IBasicServer server = ApiStack.getInterface(IBasicServer.class);
		server.startServices();

		Thread.sleep(2000L);
		assertTrue(bComplete);
		
		server.stopServices();
		Thread.sleep(2000L);
		assertEquals(IService.State.STOPPED, getService().getState());
	}

	private IService getService() {
		return ep.getExtension(TestService.class).getInstance();
		
	}
	
}
