/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiAlgs;

public class L4Test extends BasicServerTestCase {

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		getServer().enterMode(Mode.Running);
	}
	
	@Override
	protected void tearDown() throws Exception {
		getServer().leaveMode();
		super.tearDown();
	}

	@Override
	public void test() throws Exception {
		ApiAlgs.getLog(this).trace("cat default");
		
//		assertFalse(ApiAlgs.getLog(T1.class).isTraceEnabled());
		
		T1 t = new T1();
		t.init();
		t.finit();
	}
}
