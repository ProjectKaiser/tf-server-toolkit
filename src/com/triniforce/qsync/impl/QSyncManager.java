/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.db.dml.ResSet;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.qsync.intf.IQSyncManagerExternals;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.qsync.intf.QSyncQueueInfo;
import com.triniforce.qsync.intf.QSyncTaskResult;
import com.triniforce.qsync.intf.QSyncTaskStatus;
import com.triniforce.server.plugins.kernel.ep.api.IInitApi;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIPeriodicalTask;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class QSyncManager extends PKEPAPIPeriodicalTask implements IQSyncManager, IInitApi {

	private static final int DEFAULT_MAX_SYNC_TASKS = 10;
	private static final int DEFAULT_MAX_TASK_DURATION = 30000;
	private static final int EXEC_PER_HOUR = 60; // minute in hour
	private static final int DEFAULT_MAX_INIT_TASKS = 4;
	
	static abstract class SyncTask implements Runnable{
		
		protected QSyncManager m_syncMan;
		protected long m_qid;
		private long m_syncerId;
		protected IQSyncer m_syncer;
		private QSyncTaskStatus m_errorStatus;

		public SyncTask(QSyncManager sm, IQSyncer syncer, long qid, long syncerId, QSyncTaskStatus errorStatus) {
			m_syncMan = sm;
			m_qid = qid;
			m_syncerId = syncerId;
			m_syncer = syncer;
			m_errorStatus = errorStatus;
		}

		public void run() {
			QSyncTaskResult result;
			try{
				m_syncer.init();
				boolean bCompleted = exec();
				result = new QSyncTaskResult(m_qid, m_syncerId, bCompleted ? QSyncTaskStatus.SYNCED : QSyncTaskStatus.EXEC_TIMEOUT, null);
			}catch(Exception e){
				ApiAlgs.getLog(this).error("Sync task error", e);
				m_syncer.finit(e);
				result = new QSyncTaskResult(m_qid, m_syncerId, m_errorStatus, e);
			}
			m_syncMan.onTaskCompleted(result);
			SrvApiAlgs2.getIServerTran().commit();
			
		}
		
		QSyncTaskStatus getErrorStatus(){
			return m_errorStatus;
		}

		abstract boolean exec();
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + ", queue" + m_qid+ ", state" + m_errorStatus.name();
		}
		
	}
	
	static class InitialSync extends SyncTask{
		public InitialSync(QSyncManager sm, IQSyncer syncer, long qid, long syncerId) {
			super(sm, syncer, qid, syncerId, QSyncTaskStatus.INITIAL_SYNC_ERROR);
		}
		public boolean exec() {
			m_syncer.initialSync();
			return true;
			
		}
	}

	static class RecordSync extends SyncTask{
		public RecordSync(QSyncManager sm, IQSyncer syncer, long qid, long syncerId) {
			super(sm, syncer, qid,syncerId, QSyncTaskStatus.ERROR);
		}

		@Override
		boolean exec() {
			ApiAlgs.getLog(this).trace("RecordSync started");
			int timeout = m_syncMan.getMaxIncrementalSyncTaskDurationMs();
			long tst = ApiAlgs.getITime().currentTimeMillis();
			long tnd;
			Object record;
			boolean bEmpty = true;
			while(null != (record = m_syncMan.peekQueueRecord(m_qid))){
				ApiAlgs.getLog(this).trace("RecordSync : " + record);
				bEmpty = false;
				m_syncer.sync(record);
				m_syncMan.getQueueRecord(m_qid); //Record synced
				tnd = ApiAlgs.getITime().currentTimeMillis();
				if(timeout < tnd-tst){
					ApiAlgs.getLog(this).trace("time: " + tst + "("+tnd+")");
					return false;
				}				
			}
			if( bEmpty )
				ApiAlgs.getLog(this).trace("empty queue");
			
			return true;
		}
	}
	
	private int m_maxNumberOfSyncTasks = DEFAULT_MAX_SYNC_TASKS;
	private int m_maxNumberOfInitTasks = DEFAULT_MAX_INIT_TASKS;
	private int m_maxSyncTaskDurationMs = DEFAULT_MAX_TASK_DURATION;
	private IQSyncManagerExternals m_syncerExternals = new QSyncExternals();
	
	static class QSKey{
		private long m_sid;
		private long m_qid;

		public QSKey(long qid, long sid) {
			m_qid = qid;
			m_sid = sid;
		}
		
		@Override
		public String toString() {
			return String.format("q: %d, s: %d", m_qid, m_sid);
		}
		@Override
		public boolean equals(Object obj) {
			QSKey other = (QSKey) obj;
			return m_qid == other.m_qid && m_sid == other.m_sid;
		}
		
		@Override
		public int hashCode() {
			return Long.valueOf(m_qid+m_sid).hashCode();
		};
	}
	
	public static class ETaskWasNotStarted extends RuntimeException{
		private static final long serialVersionUID = 7807873920087901418L;
		public ETaskWasNotStarted(long qid) {
			super("queue: " + qid);
		}
	}
	public static class EQueueNotRegistered extends RuntimeException{
		private static final long serialVersionUID = 1L;
		public EQueueNotRegistered(long qid) {
			super("queue: " + qid);
		}
		
	}
	
	private Map<Long, QueueExecutionInfo> m_syncers = new HashMap<Long, QueueExecutionInfo>();
	
	private static class QueueExecutionInfo{
		IQSyncer m_syncer;
		
	    public long m_lastSynced;
	    public long m_lastError;
	    public long m_lastAttempt;
	    
	    SyncTask m_currentTask;
	    
	    int m_errorCounter;
	    int m_missedExecutions;
	    
	    public QueueExecutionInfo(IQSyncer syncer) {
			m_syncer = syncer;
		}
	    
	}
	
	public void setMaxNumberOfSyncTasks(int value) {
		m_maxNumberOfSyncTasks = value;
	}

	public int getMaxNumberOfSyncTasks() {
		return m_maxNumberOfSyncTasks;
	}

	public void setMaxIncrementalSyncTaskDurationMs(int value) {
		m_maxSyncTaskDurationMs = value;

	}

	public int getMaxIncrementalSyncTaskDurationMs() {
		return m_maxSyncTaskDurationMs;
	}

	public IQSyncManagerExternals getSyncerExternals() {
		return m_syncerExternals;
	}
	
	public void setSyncerExternals(IQSyncManagerExternals value) {
		m_syncerExternals = value;
	}


	synchronized public void registerQueue(long qid, long syncerId) {
		queueBL().registerQueue(qid, syncerId, QSyncTaskStatus.INITIAL_SYNC);
		IQSyncer syncer = m_syncerExternals.getQSyncer(qid, syncerId);
		syncer.connectToQueue(qid);
		m_syncers.put(qid, new QueueExecutionInfo(syncer));

	}

	synchronized public void unRegisterQueue(long qid, long syncerId) {
		queueBL().dropQueue(qid);

	}
	
	synchronized public void onEveryMinute() {
		startTasks(null);
	}

	private void startTasks(Long excludedQueue) {
		ResSet rs = null == excludedQueue ? queueBL().getQueues() : queueBL().getQueuesExclude(excludedQueue);
		int nrunning = 0;
		int nInitRunning = 0;
		
		for(QueueExecutionInfo info : m_syncers.values()){
			if(info.m_currentTask != null){
				nrunning ++;
				if(info.m_currentTask instanceof InitialSync)
					nInitRunning ++;
			}
		}
		int nexec = m_maxNumberOfSyncTasks - nrunning;
		int nExecInit = m_maxNumberOfInitTasks - nInitRunning;
		
		while(nexec > 0 && rs.next()){
			long qid = rs.getLong(1);
			long syncerId = rs.getLong(2);
			
			QueueExecutionInfo syncerInfo;
			try{
				syncerInfo = getSyncerInfo(qid, syncerId);
			} catch(IQSyncManagerExternals.EQSyncerNotFound e){
				ApiAlgs.getLog(this).error("", e);
				continue;
			}
			
			if(syncerInfo.m_currentTask != null)
				continue;
			
			QSyncTaskStatus status = QSyncTaskStatus.valueOf(rs.getString(3).trim());

			SyncTask task = null;
			
			if(EnumSet.of(QSyncTaskStatus.ERROR, QSyncTaskStatus.INITIAL_SYNC_ERROR).contains(status)){
				
				ApiAlgs.getLog(this).trace("Period: " + (Math.pow(2, (syncerInfo.m_errorCounter-1))-1));
				
				if(syncerInfo.m_missedExecutions < 
						Math.min(EXEC_PER_HOUR, 
								Math.pow(2, (syncerInfo.m_errorCounter-1))-1)){
					syncerInfo.m_missedExecutions++;
				} 
				else{
					if(QSyncTaskStatus.ERROR.equals(status))
						task = new RecordSync(this, getSyncer(qid, syncerId), qid, syncerId);
					else if(nExecInit > 0){
						task = new InitialSync(this, getSyncer(qid, syncerId), qid, syncerId);
						nExecInit --;
					}
				}
			}
			
			else if(EnumSet.of(QSyncTaskStatus.INITIAL_SYNC).contains(status)){
				if(nExecInit > 0){
					task = new InitialSync(this, getSyncer(qid, syncerId), qid, syncerId);
					nExecInit --;
				}
			}

			else if(EnumSet.of(QSyncTaskStatus.SYNCED, QSyncTaskStatus.EXEC_TIMEOUT).contains(status)){
				if(!isEmptyQueue(qid))
					task = new RecordSync(this, getSyncer(qid, syncerId), qid, syncerId);
			}
//			
//			if(QSyncTaskStatus.IN_PROCESS.equals(status)){
//				nexec --;
//			}
			
			if(null != task){
				if(startQueueTask(qid, syncerId,  task));
					nexec --;
			}
		}
		

	}

	private QueueExecutionInfo getSyncerInfo(long qid, long syncerId) {
		QueueExecutionInfo res = m_syncers.get(qid);
		
		if(null == res){
			IQSyncer syncer = m_syncerExternals.getQSyncer(qid, syncerId);
			syncer.connectToQueue(qid);
			res = new QueueExecutionInfo(syncer);
			m_syncers.put(qid, res);
		}
		return res;
	}

	private boolean startQueueTask(long qid, long sid, SyncTask task) {
		QueueExecutionInfo syncerInfo = m_syncers.get(qid);
		syncerInfo.m_lastAttempt = ApiStack.getInterface(ITime.class).currentTimeMillis(); 
		try{
			m_syncerExternals.runSync(task);
			syncerInfo.m_currentTask = task; // Task started
		}catch(Exception e){
			ApiAlgs.getLog(this).error(e.getMessage(), e);
			syncerInfo.m_lastError = ApiStack.getInterface(ITime.class).currentTimeMillis();
			queueBL().taskCompleted(new QSyncTaskResult(qid, sid, task.m_errorStatus, e)); // set error status
			return false;
		}
		return true;
		
	}

	private boolean isEmptyQueue(long qid){
		return null == IDbQueueFactory.Helper.getQueue(qid).peek(0L);
	}
	
	private Object peekQueueRecord(long qid) {
		return IDbQueueFactory.Helper.getQueue(qid).peek(0L);
	}
	
	private Object getQueueRecord(long qid) {
		return IDbQueueFactory.Helper.getQueue(qid).get(0L);
	}

	public IQSyncer getSyncer(long qid, long syncerId) {
		return m_syncers.get(qid).m_syncer;
	}

	synchronized  public boolean onQueueChanged(Long qid) {
		QSyncQueueInfo qinfo = getQueueInfo(qid);
		if(null == qinfo)
			return false;
		
		try{
			QueueExecutionInfo syncerInfo = getSyncerInfo(qid, qinfo.result.syncerId);
			if(syncerInfo.m_currentTask != null){
				ApiAlgs.getLog(this).trace("Task queue already started. Queue: "+qid);
				return true;
			}
			
			if(getRunningTasks() < getMaxNumberOfSyncTasks()){
				long syncerId = qinfo.result.syncerId;
				return startQueueTask(qid, syncerId, new RecordSync(this, syncerInfo.m_syncer, qid, syncerId));
			}
		}catch(Exception e){
			ApiAlgs.getLog(this).error(e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	private int getRunningTasks() {
		int nrunning = 0;
		
		for(QueueExecutionInfo info : m_syncers.values()){
			if(info.m_currentTask != null){
				nrunning ++;
			}
		}
		return nrunning;
	}

	synchronized public void onTaskCompleted(QSyncTaskResult result) {
		QueueExecutionInfo syncerInfo = m_syncers.get(result.qid);
		if(null == syncerInfo){
			throw new EQueueNotRegistered(result.qid);
		}
		if(null == syncerInfo.m_currentTask){
			throw new ETaskWasNotStarted(result.qid);
		}
		if(syncerInfo.m_lastAttempt < Math.max(syncerInfo.m_lastError, syncerInfo.m_lastSynced))
			throw new ETaskWasNotStarted(result.qid);
		queueBL().taskCompleted(result);
		
		if(QSyncTaskStatus.SYNCED.equals(result.status)){
			syncerInfo.m_lastSynced = ApiStack.getInterface(ITime.class).currentTimeMillis();
		}
		
		if(EnumSet.of(QSyncTaskStatus.ERROR, QSyncTaskStatus.INITIAL_SYNC_ERROR).contains(result.status)){
			syncerInfo.m_lastError = ApiStack.getInterface(ITime.class).currentTimeMillis();
			syncerInfo.m_errorCounter ++;
		}
		else{
			syncerInfo.m_missedExecutions = 0;
			syncerInfo.m_errorCounter = 0;			
		}
		
		syncerInfo.m_currentTask = null;
		
		ApiAlgs.getLog(this).trace("task completed. Status: " + result.status );
		
		Long excludeQid = null;
		if(!result.status.equals(QSyncTaskStatus.SYNCED))
			excludeQid = result.qid; 
		startTasks(excludeQid);
	}

	synchronized public QSyncQueueInfo getQueueInfo(long qid) {
		QSyncQueueInfo res = null;
		ResSet rs = queueBL().getQueueInfo(qid);
		if(rs.next()){
			res = new QSyncQueueInfo();
			QueueExecutionInfo syncerInfo = m_syncers.get(qid);
			if(null != syncerInfo){
				res.lastAttempt = syncerInfo.m_lastAttempt;
				res.lastError   = syncerInfo.m_lastError;
				res.lastSynced  = syncerInfo.m_lastSynced;
			}
			
			QSyncTaskStatus status = QSyncTaskStatus.valueOf(QSyncTaskStatus.class, rs.getString(3).trim());
			if(EnumSet.of(QSyncTaskStatus.ERROR, QSyncTaskStatus.INITIAL_SYNC_ERROR).contains(status)){
				res.result = new QSyncTaskResult(qid, rs.getLong(2), status, rs.getString(4), rs.getString(5),rs.getString(6));
			}
			else{
				res.result = new QSyncTaskResult(qid, rs.getLong(2), status, null);
			}
		}
		return res;
	}

	synchronized public List<QSyncQueueInfo> getTopQueuesInfo(int n,
			EnumSet<QSyncTaskStatus> statusToFilter) {
		List<Long> qIds = getIds();
		sortByAttempt(qIds);
		
		ArrayList<QSyncQueueInfo> res = new ArrayList<QSyncQueueInfo>();
		for(Long qid : qIds){
			QSyncQueueInfo q = getQueueInfo(qid);
			if(statusToFilter.contains(q.result.status)){
				res.add(q);
				n--;
				if(n<=0)
					break;
			}
		}
		return res;
	}

	private void sortByAttempt(List<Long> qIds) {
		Collections.sort(qIds, new Comparator<Long>() {
			public int compare(Long qid1, Long qid2) {
				QueueExecutionInfo si1 = m_syncers.get(qid1);
				QueueExecutionInfo si2 = m_syncers.get(qid2);
				int res;
				// Non created tasks first (null - no attempts)
				if(null == si1)
					res = null == si2 ? 0 : -1;
				else if (null == si2)
					res = 1;
				else
					res = Long.valueOf(si1.m_lastAttempt).compareTo(si2.m_lastAttempt); 
				return res;
			}
		});
		
	}

	private List<Long> getIds() {
		ArrayList<Long> res = new ArrayList<Long>();
		ResSet rs = queueBL().getIds();
		while(rs.next()){
			res.add(rs.getLong(1));
		}
		return res;
	}

	private TQSyncQueues.BL queueBL(){
		return SrvApiAlgs2.getIServerTran().instantiateBL(TQSyncQueues.BL.class);
	}

	public void setMaxNumberOfInitTasks(int value) {
		m_maxNumberOfInitTasks = value;
		
	}

	public int getMaxNumberOfInitTasks() {
		return m_maxNumberOfInitTasks;
	}

	@Override
	public Class getImplementedInterface() {
		return IQSyncManager.class;
	}

	@Override
	public void run() {
		onEveryMinute();
	}

	@Override
	public void initApi() {
		IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
		IPKExtensionPoint ess = (PKEPQSyncStaticSyncers) bs.getExtensionPoint(PKEPQSyncStaticSyncers.class);
		for(String ek: ess.getExtensions().keySet()){
			IPKExtension e =  ess.getExtension(ek);
			QSyncStatisSyncer ss = e.getInstance();
			long syncerId = ApiStack.getInterface(INamedDbId.class).createId(ss.getClass().getName());
			Long qId = ss.getQueueId();
			if(qId == null){
				qId = ApiStack.getInterface(INamedDbId.class).createId(ek);
				ss.setQueueId(qId);
			}
			registerQueue(qId, syncerId);
		}
	}
}
