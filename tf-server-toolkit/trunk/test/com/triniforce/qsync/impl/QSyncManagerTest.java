/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.qsync.impl.TQSyncQueues.BL;
import com.triniforce.qsync.intf.IQSyncManagerExternals;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.qsync.intf.QSyncQueueInfo;
import com.triniforce.qsync.intf.QSyncTaskResult;
import com.triniforce.qsync.intf.QSyncTaskStatus;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class QSyncManagerTest extends BasicServerRunningTestCase {
	
	static Map<Long, List<Long>> start_queue = new HashMap<Long, List<Long>>();
	static Map<Long, List<Object>> synced = new HashMap<Long, List<Object>>();
	static List<Runnable> runnables = new ArrayList<Runnable>();
	static private RuntimeException ERROR;
	static private RuntimeException EXECUTOR_START_ERROR = null;
	
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
			synced.put(qid, new ArrayList<Object>());
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

		public void sync(Object obj) {
			if(null != ERROR)
				throw ERROR;
			synced.get(m_qid).add(obj);
		}

		public void finit(Throwable t) {
			m_finitError = t;
		}

		@Override
		public void init() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	static class TestSyncExt implements IQSyncManagerExternals{

        public IQSyncer getQSyncer(long qid, Long syncerId) {
        	if(syncerId == 10001)
        		throw new EQSyncerNotFound("test");
			return new TestQSyncer(qid, null);
        }

		public void runSync(Runnable r) {
			if(null != EXECUTOR_START_ERROR)
				throw EXECUTOR_START_ERROR;
			runnables.add(r);
		}

		public void runInitialSync(Runnable r) {
			runnables.add(r);
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
		
		SrvApiAlgs2.getIServerTran().instantiateBL(TQSyncQueues.BL.class).clear();
		runnables.clear();
	}

	public void testSetMaxNumberOfSyncTasks() {
		sm.setMaxNumberOfSyncTasks(20);
		assertEquals(20, sm.getMaxNumberOfSyncTasks());
	}

	public void testGetMaxNumberOfSyncTasks() {
		assertEquals(10, sm.getMaxNumberOfSyncTasks());
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
			sm.registerQueue(6002L, 245);		
			QSyncQueueInfo res = sm.getQueueInfo(6002);
			assertEquals(245, res.result.syncerId);
			assertEquals(QSyncTaskStatus.INITIAL_SYNC, res.result.status);
	
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
			assertEquals(QSyncTaskStatus.INITIAL_SYNC,  qinfo.result.status);
			assertEquals(1, runnables.size());
			
			start_queue.put(6000L,  Arrays.asList(6L,5L,4L));
			
			execRuns();// Run initialSync
			
			assertEquals(Arrays.asList(6L,5L,4L), synced.get(6000L));
			assertEquals(QSyncTaskStatus.SYNCED, sm.getQueueInfo(6000L).result.status);
		}
		{
			sm.registerQueue(6001, 777);
			
			sm.onEveryMinute(); // should start initial sync
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(6001);
			assertEquals(QSyncTaskStatus.INITIAL_SYNC,  qinfo.result.status);
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
			sm.setMaxNumberOfSyncTasks(5);
			sm.setMaxNumberOfInitTasks(5);
			assertEquals(5, sm.getMaxNumberOfSyncTasks());
			for(int i=0; i<6; i++){
				sm.registerQueue(1000+i, 12);
			}
			
			long t1 = time.currentTimeMillis();
			sm.onEveryMinute();
			assertEquals(5, runnables.size());

			sm.onEveryMinute();
			assertEquals(5, runnables.size());
			
			long t2 = time.currentTimeMillis();
			execRun(0);
			long t2_1 = time.currentTimeMillis();
			
//			sm.onEveryMinute();
			assertEquals(5, runnables.size());
			long t3 = time.currentTimeMillis();
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(1000);
			
			assertTrue(""+qinfo.lastAttempt,  t1 <=qinfo.lastAttempt && qinfo.lastAttempt  <= t2_1);
			assertTrue(""+qinfo.lastSynced,  t1 <=qinfo.lastSynced  && qinfo.lastSynced  <= t2_1); 
			
			qinfo = sm.getQueueInfo(1005);
			assertTrue(t2 + "<"+qinfo.lastAttempt+"<"+t3,  t2 <=qinfo.lastAttempt && qinfo.lastAttempt  <= t3);
		}
		
		execRuns(); // flush runs
		
		{	//start ERROR tasks
			sm.registerQueue(300L, 80L);
			ERROR = new RuntimeException("testError");
			incExpectedLogErrorCount(1);
			sm.onEveryMinute();
			execRuns();
			QSyncQueueInfo qinfo = sm.getQueueInfo(300L);
			assertEquals(QSyncTaskStatus.INITIAL_SYNC_ERROR, qinfo.result.status);
			
			
			ERROR = null;
			sm.onEveryMinute();
			execRuns();
			qinfo = sm.getQueueInfo(300L);
			assertEquals(QSyncTaskStatus.SYNCED, qinfo.result.status);
			
		}
		
		{
			// QSyncer already registered
			BL qBL = SrvApiAlgs2.getIServerTran().instantiateBL(TQSyncQueues.BL.class);
			qBL.registerQueue(6880L, 12L, QSyncTaskStatus.INITIAL_SYNC);
			
			sm.onEveryMinute();
		}
		
		{ //QSyncExternal return exception
			SrvApiAlgs2.getIServerTran().instantiateBL(TQSyncQueues.BL.class).registerQueue(12412, 10001, QSyncTaskStatus.SYNCED);
			incExpectedLogErrorCount(1);
			sm.onEveryMinute();
				
		}
	}

	private void execRun(int i) {
		final ISrvSmartTran trn = ApiStack.getInterface(ISrvSmartTran.class);
		Mockery ctx = new Mockery();
		final ISrvSmartTran trnMock = ctx.mock(ISrvSmartTran.class);
		ApiStack.pushInterface(ISrvSmartTran.class, trnMock);
		ctx.checking(new Expectations(){{
			allowing(trnMock).instantiateBL(TQSyncQueues.BL.class); 
			will (returnValue(trn.instantiateBL(TQSyncQueues.BL.class)));
			ignoring(trnMock);
		}});
		
		Runnable r = runnables.get(0);
		r.run();
		runnables.remove(r);
	
		ApiStack.popInterface(1);
	}

	private void execRuns() {
		final ISrvSmartTran trn = ApiStack.getInterface(ISrvSmartTran.class);
		Mockery ctx = new Mockery();
		final ISrvSmartTran trnMock = ctx.mock(ISrvSmartTran.class);
		ApiStack.pushInterface(ISrvSmartTran.class, trnMock);
		ctx.checking(new Expectations(){{
			allowing(trnMock).instantiateBL(TQSyncQueues.BL.class); 
			will (returnValue(trn.instantiateBL(TQSyncQueues.BL.class)));
			ignoring(trnMock);
		}});
		ArrayList<Runnable> runs = new ArrayList<Runnable>(runnables);
		runnables.clear();
		for(Runnable r: runs){
			try{
				r.run();
			}finally{
			}
		}
//		runnables.clear();
		ApiStack.popInterface(1);
	}

	public void testOnQueueChanged() {
		m_bemu.setTimeSeq(1000, new long[]{10L});
		sm.registerQueue(555L, 101L);
		execRuns();

		putRecord(555L, 111L);
		assertTrue(sm.onQueueChanged(555L));
		execRuns();
		assertEquals(Arrays.asList(111L), synced.get(555L));
		
		
		//test duration time
		putRecord(555L, 112L);
		putRecord(555L, 113L);
		putRecord(555L, 114L);
		sm.onQueueChanged(555L);
		
		execRuns();
		
		assertEquals(Arrays.asList(111L, 112L, 113L, 114L), synced.get(555L));
		
		sm.setMaxIncrementalSyncTaskDurationMs(0);
		putRecord(555L, 115L);
		putRecord(555L, 116L);
		putRecord(555L, 117L);
		sm.onQueueChanged(555L);
		
		execRuns();
		
		assertEquals(Arrays.asList(111L, 112L, 113L, 114L, 115L), synced.get(555L));
		
		sm.setMaxIncrementalSyncTaskDurationMs(60000);
		sm.onEveryMinute();
		
		assertFalse(sm.onQueueChanged(15321563L));
		
		{ // already started task
			sm.registerQueue(790L, 123L);
			sm.onEveryMinute(); // start InitTask
			long tst = ApiStack.getInterface(ITime.class).currentTimeMillis();
			assertTrue(sm.onQueueChanged(790L));
			execRuns();
			assertTrue(tst > sm.getQueueInfo(790L).lastAttempt);
		}
		
		{
			{
				QSyncManager sm2 = new QSyncManager(); // update QueueBL
				syncExt = new TestSyncExt();
				sm2.setSyncerExternals(syncExt);

				sm2.registerQueue(83485L, 346L);
			}
			
			sm.onQueueChanged(83485L);
		}
		
		{
			execRuns();
			sm.onEveryMinute();
			assertTrue(runnables.toString(), runnables.isEmpty());
		
			assertEquals(10, sm.getMaxNumberOfSyncTasks());
			for(int i=0; i<11; i++){
				long qid = 556+ i;
				sm.registerQueue(qid, 1);
				sm.onQueueChanged(qid);
			}
			assertEquals(runnables.toString(), 10, runnables.size());
		}
		
		{// If 
			
		}
	}

	private void putRecord(long qId, long rId) {
		IDbQueueFactory.Helper.getQueue(qId).put(rId);
		
	}

	public void testOnTaskCompleted() {
		ITime time = ApiStack.getInterface(ITime.class);
		
		sm.registerQueue(5555L, 101L);
		long t1,t2,t3,t4;
		{
			sm.onEveryMinute(); // start task 
			QSyncTaskResult result = new QSyncTaskResult(5555L, 101L, QSyncTaskStatus.SYNCED, null);
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
			putRecord(5555L, 900L);
			sm.onQueueChanged(5555L);
			runnables.clear();
			
			QSyncTaskResult result = new QSyncTaskResult(5555L, 101L, QSyncTaskStatus.ERROR, null);
			
			t3 = time.currentTimeMillis();
			sm.onTaskCompleted(result);
			t4 = time.currentTimeMillis();
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(5555L);
			assertTrue(""+qinfo.lastSynced,  t1 <=qinfo.lastSynced  && qinfo.lastSynced  <= t2); 
			assertTrue(""+qinfo.lastError,  t3 <=qinfo.lastError && qinfo.lastError  <= t4); 
			
		}
		{// task was not started
			sm.registerQueue(5556L, 101L);
			QSyncTaskResult result = new QSyncTaskResult(5555L, 101L, QSyncTaskStatus.ERROR, null);
			try{
				sm.onTaskCompleted(result);
				fail();
			}catch(QSyncManager.ETaskWasNotStarted e ){
			}
			
			sm.onEveryMinute();
			runnables.clear();
			result.status = QSyncTaskStatus.SYNCED;
			sm.onTaskCompleted(result);
			
			try{
				sm.onTaskCompleted(result);
				fail();
			}catch(QSyncManager.ETaskWasNotStarted e ){
			}
		}
		
		// After task completed try to start next 
		{
			sm.registerQueue(5557L, 101L);
			sm.registerQueue(5558L, 101L);
			QSyncTaskResult result = new QSyncTaskResult(5555L, 101L, QSyncTaskStatus.SYNCED, null);
			result.qid = 5557L;
			result.syncerId = 101L;
			result.status = QSyncTaskStatus.SYNCED;
			sm.onQueueChanged(5557L);
			
			long t21 = ApiStack.getInterface(ITime.class).currentTimeMillis();
			
			sm.onTaskCompleted(result);
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(5558L);
			assertTrue(""+qinfo.lastAttempt, t21 < qinfo.lastAttempt);
			
		}

	}

	public void testGetQueueInfo() {
		sm.registerQueue(6000, 777);
		assertEquals(QSyncTaskStatus.INITIAL_SYNC,  sm.getQueueInfo(6000).result.status);
	}

	public void testGetTopQueuesInfo() {
		INamedDbId ids = ApiStack.getInterface(INamedDbId.class);
		long sid = ids.createId("testGetTopQueuesInfo.syncer");
		long qids[];
		{
			qids = new long[]{
					ids.createId("testGetTopQueuesInfo.q1"),
					ids.createId("testGetTopQueuesInfo.q2"),
					ids.createId("testGetTopQueuesInfo.q3"),
					ids.createId("testGetTopQueuesInfo.q4")
			};
		}

		
		sm.registerQueue(qids[0], sid);
		sm.registerQueue(qids[1], sid);
		sm.registerQueue(qids[2], sid);
		
		List<QSyncQueueInfo> res = sm.getTopQueuesInfo(3, EnumSet.of(QSyncTaskStatus.INITIAL_SYNC));
		assertEquals(3, res.size());
		assertEquals(qids[1], res.get(1).result.qid);
		
		sm.onEveryMinute();
		execRuns();
		
		sm.registerQueue(qids[3],sid);
		res = sm.getTopQueuesInfo(3, EnumSet.of(QSyncTaskStatus.INITIAL_SYNC));
		assertEquals(1, res.size());
		assertEquals(qids[3], res.get(0).result.qid);

		
		res = sm.getTopQueuesInfo(2, EnumSet.of(QSyncTaskStatus.INITIAL_SYNC, QSyncTaskStatus.SYNCED));
		assertEquals(2, res.size());
		assertEquals(qids[3], res.get(0).result.qid);
		
		
		String format = "queue(queueId)\tsyncer(syncerId)\tstatus\tattempt - error - synced ";
		DateFormat df = DateFormat.getInstance();
		res = sm.getTopQueuesInfo(100, EnumSet.allOf(QSyncTaskStatus.class));
		for (QSyncQueueInfo info : res) {
			String str = format;
			str = str.replaceFirst("queue", ids.getName(info.result.qid));
			str = str.replaceFirst("queueId", Long.valueOf(info.result.qid).toString());
			str = str.replaceFirst("syncer", ids.getName(info.result.syncerId));
			str = str.replaceFirst("syncerId", Long.valueOf(info.result.syncerId).toString());
			str = str.replaceFirst("status", info.result.status.name());
			
			str = str.replaceFirst("attempt",df.format(info.lastAttempt) );
			str = str.replaceFirst("error",df.format(info.lastError) );
			str = str.replaceFirst("synced",df.format(info.lastSynced) );
			trace(str);
			if(null != info.result.errorClass)
				trace(info.result.toString());
		}
		

	}
	
	public void testErrorExecution(){
		
		{
			incExpectedLogErrorCount(1);
			ERROR = new RuntimeException("testErrorExceution");
			
			sm.registerQueue(555L, 1L);
			sm.onEveryMinute();
			execRuns();
			
			QSyncQueueInfo qinfo = sm.getQueueInfo(555L);
			assertEquals(QSyncTaskStatus.INITIAL_SYNC_ERROR, qinfo.result.status);
			assertEquals(RuntimeException.class.getName(), qinfo.result.errorClass);
			assertEquals("testErrorExceution", qinfo.result.errorMessage);
			
			String[] lines = qinfo.result.errorStack.split("\n");
			assertEquals("java.lang.RuntimeException: testErrorExceution", lines[0].trim());
			assertEquals("at com.triniforce.qsync.impl.QSyncManage", lines[1].trim().substring(0,40));
			
			TestQSyncer s  = (TestQSyncer) sm.getSyncer(555L, 1L); // Syncer finited
			assertSame(ERROR, s.m_finitError);
			
			ERROR = null;
			sm.onEveryMinute();
			execRuns();
			qinfo = sm.getQueueInfo(555L);
			assertEquals(QSyncTaskStatus.SYNCED, qinfo.result.status);
			assertEquals(null, qinfo.result.errorClass);
			assertEquals(null, qinfo.result.errorMessage);
			
			
			incExpectedLogErrorCount(1);
			ERROR = new RuntimeException("testErrorExceution_InSYNC");
			
			putRecord(555L, 564L);
			sm.onQueueChanged(555L);
			execRuns();
			
			qinfo = sm.getQueueInfo(555L);
			assertEquals(QSyncTaskStatus.ERROR, qinfo.result.status);
			assertEquals(RuntimeException.class.getName(), qinfo.result.errorClass);
			assertEquals("testErrorExceution_InSYNC", qinfo.result.errorMessage);
		}
		
		{	// If Error occured next try should be executed with doubled interval
			sm.unRegisterQueue(555L, 1L);
			incExpectedLogErrorCount(4);
			sm.registerQueue(540L, 10L);
			ERROR = new RuntimeException();
			execTry(); // execute
			execTry(); // execute
			long t1 = ApiStack.getInterface(ITime.class).currentTimeMillis();
			execTry(); // no execute
			assertTrue(t1 > sm.getQueueInfo(540L).lastAttempt);
			execTry(); // execute
			execTry(); // no execute
			long t2 = ApiStack.getInterface(ITime.class).currentTimeMillis();
			execTry(); // no execute
			assertTrue(t2 > sm.getQueueInfo(540L).lastAttempt);
			execTry(); // execute
			execTry(); // no execute
		}
		
	}

	public void testRejectedExecutionException(){
		
	}
	
	private void execTry() {
		sm.onEveryMinute();
		execRuns();
	}

	
	public void testMaxInitTasks(){
		sm.setMaxNumberOfInitTasks(2);
		
		sm.registerQueue(222, 1);
		sm.registerQueue(223, 1);
		sm.registerQueue(224, 1);
		sm.onEveryMinute();
		
		assertEquals(2, runnables.size());
		
		sm.onEveryMinute();
		assertEquals(2, runnables.size());
	}
	
	
	public void testRecordSync(){
		putRecord(555L, 564L);
		sm.registerQueue(555L, 100L);
		sm.onQueueChanged(555L);
		ERROR = new RuntimeException();
		incExpectedLogErrorCount(1);
		execRuns();
		assertEquals(564L, IDbQueueFactory.Helper.getQueue(555L).get(0L));
	}
	
	public void testStartTask(){
		sm.registerQueue(1000L, 100L);
		EXECUTOR_START_ERROR = new RuntimeException("testStartTask");
		long tst = ApiStack.getInterface(ITime.class).currentTimeMillis();
		assertFalse(sm.onQueueChanged(1000L));
		QSyncQueueInfo qinfo = sm.getQueueInfo(1000L);
		assertTrue(qinfo.lastError > tst);
		tst = ApiStack.getInterface(ITime.class).currentTimeMillis();  
		assertFalse(sm.onQueueChanged(1000L));
		qinfo = sm.getQueueInfo(1000L);
		assertTrue(qinfo.lastError > tst);
		incExpectedLogErrorCount(2);
		EXECUTOR_START_ERROR = null;
		
		assertNotNull(qinfo.result.errorMessage);
	}
	
}
