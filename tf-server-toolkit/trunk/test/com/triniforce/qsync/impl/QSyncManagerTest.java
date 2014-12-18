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

public class QSyncManagerTest extends BasicServerRunningTestCase {
	
	static Map<Long, List<Long>> start_queue = new HashMap<Long, List<Long>>();
	static Map<Long, List<Long>> synced = new HashMap<Long, List<Long>>();
	static List<Runnable> runnables = new ArrayList<Runnable>();
	
	static class TestQSyncer implements IQSyncer{

		private long m_qid;
//		private Class<IQSyncer> m_sc;

		public TestQSyncer(long qid, Class<IQSyncer> syncClass) {
			m_qid = qid;
//			m_sc = syncClass;
		}

		public void connectToQueue(long qid) {
			assertEquals(m_qid, qid);
			synced.put(qid, new ArrayList<Long>());
		}

		public void initialSync() {
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
			// TODO Auto-generated method stub
			
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
	}

	public void testSetMaxNumberOfSyncTasks() {
		sm.setMaxNumberOfSyncTasks(20);
		assertEquals(20, sm.getMaxNumberOfSyncTasks());
	}

	public void testGetMaxNumberOfSyncTasks() {
		assertEquals(5, sm.getMaxNumberOfSyncTasks());
	}

	public void testSetMaxSyncTaskDurationMs() {
		sm.setMaxSyncTaskDurationMs(3000);
		assertEquals(3000, sm.getMaxSyncTaskDurationMs());
	}

	public void testGetMaxSyncTaskDurationMs() {
		assertEquals(30000, sm.getMaxSyncTaskDurationMs());
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
		{
			
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
	}

	public void testOnTaskCompleted() {
		sm.registerQueue(5555L, 101L);
		
		QSyncTaskResult result = new QSyncTaskResult();
		result.qid = 5555L;
		result.syncerId = 101L;
		result.status = QSyncTaskStatus.SYNCED;
		
		sm.onTaskCompleted(result);
		
		assertEquals(QSyncTaskStatus.SYNCED, sm.getQueueInfo(5555L).result.status);
	}

	public void testGetQueueInfo() {
		sm.registerQueue(6000, 777);
		assertEquals(QSyncTaskStatus.NOT_STARTED,  sm.getQueueInfo(6000).result.status);
	}

	public void testGetTopQueuesInfo() {
		sm.getTopQueuesInfo(1245, 1, EnumSet.of(QSyncTaskStatus.NOT_STARTED));
	}

}
