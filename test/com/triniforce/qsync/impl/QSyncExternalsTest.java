/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.qsync.impl.QSyncPlugin.TestSyncer;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.INamedDbId.ENotFound;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.utils.ApiStack;

public class QSyncExternalsTest extends BasicServerTestCase {


	
	@Override
	protected void setUp() throws Exception {
		
		addPlugin(new QSyncPlugin());
		super.setUp();
		
		getServer().enterMode(Mode.Running);
	}
	
	@Override
	protected void tearDown() throws Exception {
		getServer().leaveMode();
		super.tearDown();
	}
	
	public void testGetQSyncer() throws Exception {
		QSyncExternals ext = new QSyncExternals();
		try{
			assertNull(ext.getQSyncer(188L, 999L));
			fail();
		}catch(ENotFound e){}
		
		Long sId;
		assertNotNull(sId = ApiStack.getInterface(INamedDbId.class).queryId(TestSyncer.class.getName()));
		IQSyncer res = ext.getQSyncer(40003L, sId);
		assertNotNull(res);
	}
	
	boolean complete = false;
	
	@Override
	public void test() throws Exception {
		QSyncExternals ext = new QSyncExternals();
		ITaskExecutors te = ApiStack.getInterface(ITaskExecutors.class);
		int before = te.getTasksCount();

		ext.runSync(new Runnable(){
			public void run() {}
		});
		
		assertEquals(before+1, te.getTasksCount());
		Thread.sleep(100L);
	}
	
	public void testRunTask() throws InterruptedException{
		QSyncExternals ext = new QSyncExternals();
		ext.runSync(new Runnable(){
			public void run() {
				complete = true;
			}
		});
		Thread.sleep(500L);
		assertTrue(complete);

		complete = false;
		ext.runSync(new Runnable(){
			public void run() {
				complete = true;
			}
			
		});
		Thread.sleep(500L);
		assertTrue(complete);

	}
}
