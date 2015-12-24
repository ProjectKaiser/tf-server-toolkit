/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IFiniter;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.ILocker;
import com.triniforce.server.srvapi.ISrvSmartTranExtenders.IRefCountHashMap;
import com.triniforce.utils.ApiStack;

public class ServerTransationFactoryTest extends BasicServerTestCase {
	
	@Override
	public void test() throws Exception {
		getServer().enterMode(Mode.Running);
		try{
			ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
			{	// test that interface installed by transaction extender
				assertNotNull(ApiStack.getApi().queryIntfImplementor(IRefCountHashMap.class));
				assertNotNull(ApiStack.getApi().queryIntfImplementor(IFiniter.class));
				assertNotNull(ApiStack.getApi().queryIntfImplementor(ILocker.class));
				trf.pop();
				try{
					assertNull(ApiStack.getApi().queryIntfImplementor(IRefCountHashMap.class));
					assertNull(ApiStack.getApi().queryIntfImplementor(IFiniter.class));
					assertNull(ApiStack.getApi().queryIntfImplementor(ILocker.class));
				} finally{
					trf.push();
				}
			}
		}finally{
			getServer().leaveMode();
		}
	}
}
