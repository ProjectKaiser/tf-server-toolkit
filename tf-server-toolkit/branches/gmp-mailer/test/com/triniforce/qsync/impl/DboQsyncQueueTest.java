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
import com.triniforce.server.srvapi.ITaskExecutors;
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
		
		getServer().enterMode(Mode.Running);
		try{
			ApiStack.getInterface(ITaskExecutors.class).awatTermination(1000L);
		}finally{
			getServer().leaveMode();
		}
	}

	@Override
	public void test() throws Exception {
		synchronized (QSyncPlugin.syncObj) {
			putQueue(40001L, "str_01");
			QSyncPlugin.syncObj.wait();
		}
		
		TestSyncer s = p.getSyncer(getServer());
		
		getServer().enterMode(Mode.Running);
		try{
			QSyncManager sm = (QSyncManager) ApiStack.getInterface(IQSyncManager.class);
			QSyncExternals sme = (QSyncExternals) sm.getSyncerExternals();
			sme.waitForTaskCompletition();
		}finally{
			getServer().leaveMode();
		}

		QSyncTaskStatus qsState;
		trace("Read queue status");
		getServer().enterMode(Mode.Running);
		try{
			qsState = ApiStack.getInterface(IQSyncManager.class).getQueueInfo(40001L).result.status; 
		}finally{
			getServer().leaveMode();
		}
		
		assertEquals(QSyncTaskStatus.SYNCED, qsState);
		
		synchronized (QSyncPlugin.syncObj) {
			putQueue(40001L, "str_02");
			QSyncPlugin.syncObj.wait();
		}
		
		assertEquals(Arrays.asList("str_01", "str_02"), s.synced());
		
		
		getServer().enterMode(Mode.Running);
		try{
			ApiStack.getInterface(ITaskExecutors.class).awatTermination(1000L);
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
