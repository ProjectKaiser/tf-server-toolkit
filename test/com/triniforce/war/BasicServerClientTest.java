/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.lang.reflect.Proxy;
import java.net.URL;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.war.testpkg.ISvc1;

public class BasicServerClientTest extends TFTestCase {


	private URL url;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	public void ntest() throws Exception {
		url = new URL("http","localhost", BasicServerJetty.PORT, "/bserv");
		ISvc1 res = (ISvc1) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ISvc1.class}, new BasicServerClient(url, ISvc1.class));
		assertEquals("TESTRES: 12341", res.method1(12341));
	}
	

}
