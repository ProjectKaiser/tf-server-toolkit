/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.srvapi.IBasicServer;

public class BasicServerTestCaseTest extends BasicServerTestCase {
	
	static class TestPlugin extends PKPlugin{

		@Override
		public void doRegistration() {
		}

		@Override
		public void doExtensionPointsRegistration() {
		}
		
	}
	
	@Override
	protected void setUp() throws Exception {
		addPlugin(new TestPlugin());
		super.setUp();
	}
	
	static IBasicServer srv1;

	public void test1(){
		if(null != srv1){
			assertSame(srv1, getServer());
		}
		else
			srv1 = getServer();
	}
	
	public void test2(){
		if(null != srv1){
			assertSame(srv1, getServer());
		}
		else
			srv1 = getServer();
		
	}
	
	
}
