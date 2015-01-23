/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.util.Arrays;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.qsync.impl.QSyncPlugin.TestSyncer;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.qsync.intf.QSyncTaskStatus;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiStack;

public class DboQsyncQueueTest extends BasicServerTestCase {

	private QSyncPlugin p;

	@Override
	protected void setUp() throws Exception {
		
		addPlugin(p = new QSyncPlugin());

		super.setUp();
		
		getServer().enterMode(Mode.Running);
		try{
			IDbQueueFactory.Helper.cleanQueue(40001L);
			ApiStack.getInterface(ISrvSmartTran.class).commit();
		}finally{
			getServer().leaveMode();
		}
	}

	@Override
	public void test() throws Exception {
		putQueue(40001L, "str_01");
		runSyncMan();
		
		TestSyncer s = p.getSyncer(getServer());

		Thread.sleep(100L);
		
		getServer().enterMode(Mode.Running);
		try{
			assertEquals(QSyncTaskStatus.SYNCED, 
					ApiStack.getInterface(IQSyncManager.class).getQueueInfo(40001L).result.status);
		}finally{
			getServer().leaveMode();
		}
		
		
		putQueue(40001L, "str_02");
		runSyncMan();
		Thread.sleep(100L);
		
		assertEquals(Arrays.asList("str_01", "str_02"), s.synced());
	}

	private void runSyncMan() {
		getServer().enterMode(Mode.Running);
		try{
			ApiStack.getInterface(IQSyncManager.class).onEveryMinute();
			SrvApiAlgs2.getIServerTran().commit();
		}finally{
			getServer().leaveMode();
		}
	}

	private void putQueue(long qId, String string) {
		getServer().enterMode(Mode.Running);
		try{
			IDbQueueFactory.Helper.getQueue(qId).put(string);
			ApiStack.getInterface(ISrvSmartTran.class).commit();
		}finally{
			getServer().leaveMode();
		}
		
	}

}
