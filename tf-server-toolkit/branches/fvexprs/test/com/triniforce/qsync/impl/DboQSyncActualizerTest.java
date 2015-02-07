/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.qsync.intf.QSyncQueueInfo;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class DboQSyncActualizerTest extends BasicServerTestCase {
	
	@Override
	protected void setUp() throws Exception {
		addPlugin(new QSyncPlugin());

		super.setUp();
		
		
	}
	
	@Override
	public void test() throws Exception {
		getServer().enterMode(Mode.Running);
		try{
			QSyncQueueInfo info = ApiStack.getInterface(IQSyncManager.class).getQueueInfo(40001L);
			assertNotNull(info);
		}finally{
			getServer().leaveMode();
		}
	}
}
