/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.util.concurrent.ExecutionException;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.qsync.impl.QSyncPlugin.TestSyncer;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
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
		}catch(com.triniforce.qsync.intf.IQSyncManagerExternals.EQSyncerNotFound e){}
		
		
		
		Long sId;
		sId = ApiStack.getInterface(INamedDbId.class).createId(TestSyncer.class.getName());
		IQSyncer res = ext.getQSyncer(40003L, sId);
		assertNotNull(res);
	}
	
	boolean complete = false;

	Object obj = new Object();
	
	@Override
	public void test() throws Exception {
		QSyncExternals ext = new QSyncExternals();
		ITaskExecutors te = ApiStack.getInterface(ITaskExecutors.class);
		int before = te.getTasksCount();

		ext.runSync(new Runnable(){
			public void run() {
				synchronized (obj) {
					obj.notify();
				}
			}
		});
		
		assertEquals(before+1, te.getTasksCount());
		
		synchronized (obj) {
			obj.wait(0);
			
		}
		te.awatTermination(100L);
		
		ext.waitForTaskCompletition();
	}
	
	
	public void testRunTask() throws InterruptedException, ExecutionException{
		QSyncExternals ext = new QSyncExternals();
		synchronized (obj) {
			ext.runSync(new Runnable(){
				public void run() {
					complete = true;
					synchronized (obj) {
						obj.notify();
					}
					
				}
			});
			obj.wait();
		}
		assertTrue(complete);

		synchronized (obj) {
			complete = false;
			ext.runSync(new Runnable(){
				public void run() {
					complete = true;
					synchronized (obj) {
						obj.notify();
					}
				}
				
			});
			obj.wait();
		}
		assertTrue(complete);
		ext.waitForTaskCompletition();
	}
}
