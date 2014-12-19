/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.qsync.intf.IQSyncManagerExternals;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.qsync.intf.QSyncQueueInfo;
import com.triniforce.qsync.intf.QSyncTaskResult;
import com.triniforce.qsync.intf.QSyncTaskStatus;
import com.triniforce.server.TFPlugin;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class QSyncManagerTest extends BasicServerRunningTestCase {
	
	static Map<Long, List<Long>> start_queue = new HashMap<Long, List<Long>>();
	static Map<Long, List<Long>> synced = new HashMap<Long, List<Long>>();
	static List<Runnable> runnables = new ArrayList<Runnable>();
	static private RuntimeException ERROR;
	
	static class TestQSyncer implements IQSyncer{

		private long m_qid;
//		private Class<IQSyncer> m_sc;
		private Throwable m_finitError;

		public TestQSyncer(long qid, Class<IQSyncer> syncClass) {
			m_qid = qid;
//			m_sc = syncClass;
		}

		public void connectToQueue(long qid) {
			assertEquals(m_qid, qid);
			synced.put(qid, new ArrayList<Long>());
		}

		public void initialSync() {
			if(null != ERROR)
				throw ERROR;
			List<Long> q = start_queue.get(m_qid);
			if(null != q){
				synced.get(m_qid).addAll(q);
				start_queue.remove(m_qid);
			}
			
		}

		public void sync(long recordId) {
			synced.get(m_qid).add(recordId);
		}

		public void finit(Throwable t) {
			m_finitError = t;
		}
		
	}
	
	static class TestSyncExt implements IQSyncManagerExternals{
		public IQSyncer getQSyncer(long qid, Class<IQSyncer> syncClass) {
			return new TestQSyncer(qid, syncClass);
		}

		public void runTask(Runnable r) {
			runnables.add(r);
		}
	}
	
	static class QSyncPlugin extends TFPlugin{

		@Override
		public void doRegistration() {
			putExtension(PKEPDBObjects.class, TQSyncQueues.class);
		}

		@Override
		public void doExtensionPointsRegistration() {
			
		}
		
	}
	
	private QSyncManager sm;
	private TestSyncExt syncExt;

	@Override
	protected void setUp() throws Exception {
		addPlugin(new QSyncPlugin());
		super.setUp();
		sm = new QSyncManager();
		syncExt = new TestSyncExt();
		sm.setSyncerExternals(syncExt);
		ERROR = null;
	}

	public void testSetMaxNumberOfSyncTasks() {
		sm.setMaxNumberOfSyncTasks(20);
		assertEquals(20, sm.getMaxNumberOfSyncTasks());
	}

	public void testGetMaxNumberOfSyncTasks() {
		assertEquals(5, sm.getMaxNumberOfSyncTasks());
	}

	public void testSetMaxSyncTaskDurationMs() {
		sm.setMaxIncrementalSyncTaskDurationMs(3000);
		assertEquals(3000, sm.getMaxIncrementalSyncTaskDurationMs());
	}

	public void testGetMaxSyncTaskDurationMs() {
		assertEquals(30000, sm.getMaxIncrementalSyncTaskDurationMs());
	}

	public void testGetSyncerExternals() {
		IQSyncManagerExternals res = sm.getSyncerExternals();
		assertSame(syncExt, res);
	}

	public void testRegisterQueue() {
		{
			// test queue id
			sm.registerQueue(6000, 222);
			
			QSyncQueueInfo res = sm.getQueueInfo(6000);
			assertNotNull(res);
			
			assertNull(sm.getQueueInfo(6001));
		}
		{
			//test syncer id
			sm.registerQueue(6002, 244);
			QSyncQueueInfo res = sm.getQueueInfo(6002);
			assertEquals(244, res.result.syncerId);
			assertEquals(222, sm.getQueueInfo(6000).result.syncerId);
			
			assertTrue(synced.keySet().toString(), synced.keySet().contains(6002L));
		}
		
		{
			//already registered
			try{
				sm.registerQueue(6002L, 245);
				fail();
			}catch(Exception e){}
		}
	}

	public void testUnRegisterQueue() {
		sm.registerQueue(4000, 123);
		sm.registerQueue(4001, 123);
		sm.unRegisterQueue(4000, 123);
		assertNull(sm.getQueueInfo(4000));
		assertNotNull(sm.getQueueInfo(4001));
	}

	public void testOnEveryMinute() {
		{
			sm.registerQueue(6000, 777);
			assertNotNull(synced.get(6000L));
			
			sm.onEveryMinute(); // should start initial sync
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(6000);
			assertEquals(QSyncTaskStatus.IN_PROCESS,  qinfo.result.status);
			assertEquals(1, runnables.size());
			
			start_queue.put(6000L,  Arrays.asList(6L,5L,4L));
			execRuns();// Run initialSync
			
			assertEquals(Arrays.asList(6L,5L,4L), synced.get(6000L));
		}
		{
			sm.registerQueue(6001, 777);
			
			sm.onEveryMinute(); // should start initial sync
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(6001);
			assertEquals(QSyncTaskStatus.IN_PROCESS,  qinfo.result.status);
		}
		{
			start_queue.put(3002L, Arrays.asList(10L));
			
			sm.registerQueue(3001L, 12L);
			sm.registerQueue(3002L, 13L);
			sm.registerQueue(3003L, 14L);
			
			sm.onEveryMinute();
			
			execRuns();
			
			assertEquals(Arrays.asList(), synced.get(3003L));
			assertEquals(Arrays.asList(10L), synced.get(3002L));
			
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(3002L);
			assertEquals(QSyncTaskStatus.SYNCED, qinfo.result.status);
		}
		ITime time = ApiStack.getInterface(ITime.class);
		{//test timings
			sm.registerQueue(6667L, 13L);
			long t1 = time.currentTimeMillis();
			sm.onEveryMinute();
			long t2 = time.currentTimeMillis();
			execRuns();
			long t3 = time.currentTimeMillis();
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(6667L);
			assertTrue(""+qinfo.lastAttempt, t1 <=qinfo.lastAttempt && qinfo.lastAttempt <= t2); 
			assertTrue(""+qinfo.lastSynced,  t2 <=qinfo.lastSynced  && qinfo.lastSynced  <= t3); 
		}
		
		{// MaxNumberOfSyncTask parameter
			assertEquals(5, sm.getMaxNumberOfSyncTasks());
			for(int i=0; i<6; i++){
				sm.registerQueue(1000+i, 12);
			}
			
			long t1 = time.currentTimeMillis();
			sm.onEveryMinute();
			assertEquals(5, runnables.size());

			sm.onEveryMinute();
			assertEquals(5, runnables.size());
			
			runnables.get(0).run();
			runnables.remove(0);
			long t2 = time.currentTimeMillis();
			
			sm.onEveryMinute();
			assertEquals(5, runnables.size());
			long t3 = time.currentTimeMillis();
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(1000);
			
			assertTrue(""+qinfo.lastAttempt,  t1 <=qinfo.lastAttempt && qinfo.lastAttempt  <= t2);
			assertTrue(""+qinfo.lastSynced,  t1 <=qinfo.lastSynced  && qinfo.lastSynced  <= t2); 
			
			qinfo = sm.getQueueInfo(1005);
			assertTrue(""+qinfo.lastAttempt,  t2 <=qinfo.lastAttempt && qinfo.lastAttempt  <= t3);
		}
	}

	private void execRuns() {
		for (Runnable r : runnables) {
			r.run();
		}
		runnables.clear();
	}

	public void testOnRecordChanged() {
		sm.registerQueue(555L, 101L);
		sm.onEveryMinute(); // initSync
		execRuns();
		
		sm.onRecordChanged(555L, 111L);
		sm.onEveryMinute();// recordSync
		execRuns();
		assertEquals(Arrays.asList(111L), synced.get(555L));
		
		
		//test duration time
		sm.onRecordChanged(555L, 112L);
		sm.onRecordChanged(555L, 113L);
		sm.onRecordChanged(555L, 114L);
		sm.onEveryMinute();// recordSync
		
		execRuns();
		
		assertEquals(Arrays.asList(111L, 112L, 113L, 114L), synced.get(555L));
		
		sm.setMaxIncrementalSyncTaskDurationMs(0);
		sm.onRecordChanged(555L, 115L);
		sm.onRecordChanged(555L, 116L);
		sm.onRecordChanged(555L, 117L);
		sm.onEveryMinute();// recordSync
		
		execRuns();
		
		assertEquals(Arrays.asList(111L, 112L, 113L, 114L, 115L), synced.get(555L));
	}

	public void testOnTaskCompleted() {
		ITime time = ApiStack.getInterface(ITime.class);
		
		sm.registerQueue(5555L, 101L);
		long t1,t2,t3,t4;
		{
			sm.onEveryMinute(); // start task 
			QSyncTaskResult result = new QSyncTaskResult();
			result.qid = 5555L;
			result.syncerId = 101L;
			result.status = QSyncTaskStatus.SYNCED;
			runnables.clear();
			
			t1 = time.currentTimeMillis();
			sm.onTaskCompleted(result);
			t2 = time.currentTimeMillis();
			
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(5555L);
			assertEquals(QSyncTaskStatus.SYNCED, qinfo.result.status);
			assertTrue(""+qinfo.lastSynced,  t1 <=qinfo.lastSynced  && qinfo.lastSynced  <= t2); 
			assertEquals(0L, qinfo.lastError);
		}
		
		{
			sm.onRecordChanged(5555L, 900L);
			sm.onEveryMinute();
			runnables.clear();
			
			QSyncTaskResult result = new QSyncTaskResult();
			result.qid = 5555L;
			result.syncerId = 101L;
			result.status = QSyncTaskStatus.ERROR;
			
			t3 = time.currentTimeMillis();
			sm.onTaskCompleted(result);
			t4 = time.currentTimeMillis();
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(5555L);
			assertTrue(""+qinfo.lastSynced,  t1 <=qinfo.lastSynced  && qinfo.lastSynced  <= t2); 
			assertTrue(""+qinfo.lastError,  t3 <=qinfo.lastError && qinfo.lastError  <= t4); 
			
		}
		{// task was not started
			sm.registerQueue(5556L, 101L);
			QSyncTaskResult result = new QSyncTaskResult();
			result.qid = 5556L;
			result.syncerId = 101L;
			result.status = QSyncTaskStatus.ERROR;
			try{
				sm.onTaskCompleted(result);
				fail();
			}catch(QSyncManager.ETaskWasNotStarted e ){
			}
			
			sm.onEveryMinute();
			runnables.clear();
			sm.onTaskCompleted(result);
			
			try{
				sm.onTaskCompleted(result);
				fail();
			}catch(QSyncManager.ETaskWasNotStarted e ){
			}
		}

	}

	public void testGetQueueInfo() {
		sm.registerQueue(6000, 777);
		assertEquals(QSyncTaskStatus.NOT_STARTED,  sm.getQueueInfo(6000).result.status);
	}

	public void testGetTopQueuesInfo() {
		sm.getTopQueuesInfo(1, EnumSet.of(QSyncTaskStatus.NOT_STARTED));
	}
	
	public void testErrorExecution(){
		ERROR = new RuntimeException("testErrorExceution");
		
		sm.registerQueue(555L, 1L);
		sm.onEveryMinute();
		execRuns();
		
		QSyncQueueInfo qinfo = sm.getQueueInfo(555L);
		assertEquals(QSyncTaskStatus.ERROR, qinfo.result.status);
		assertEquals(RuntimeException.class.getName(), qinfo.result.errorClass);
		assertEquals("testErrorExceution", qinfo.result.errorMessage);
		
		String[] lines = qinfo.result.errorStack.split("\n");
		assertEquals("java.lang.RuntimeException: testErrorExceution", lines[0].trim());
		assertEquals("at com.triniforce.qsync.impl.QSyncManage", lines[1].trim().substring(0,40));
		
		TestQSyncer s  = (TestQSyncer) sm.getSyncer(555L, 1L); // Syncer finited
		assertSame(ERROR, s.m_finitError);
	}

}
