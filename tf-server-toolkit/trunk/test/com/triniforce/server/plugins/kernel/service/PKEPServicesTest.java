/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.service;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class PKEPServicesTest extends BasicServerTestCase {
	
	static boolean bComplete = false;
	
	public static class TestService extends EPService{
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
		
        EP_IThreadWatcherRegistrator twr = ApiStack.getInterface(EP_IThreadWatcherRegistrator.class);
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

		Thread.sleep(200L);
		assertTrue(bComplete);
		
		server.stopServices();
		assertEquals(EP_IService.State.STOPPED, getService().getState());
	}

	private EP_IService getService() {
		return ep.getExtension(TestService.class).getInstance();
		
	}
	
}
