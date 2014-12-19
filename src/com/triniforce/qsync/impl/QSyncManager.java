/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.db.dml.ResSet;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.qsync.intf.IQSyncManagerExternals;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.qsync.intf.QSyncQueueInfo;
import com.triniforce.qsync.intf.QSyncTaskResult;
import com.triniforce.qsync.intf.QSyncTaskStatus;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class QSyncManager implements IQSyncManager {

	private static final int DEFAULT_MAX_SYNC_TASKS = 5;
	private static final int DEFAULT_MAX_TASK_DURATION = 30000;
	
	static abstract class SyncTask implements Runnable{
		
		private IQSyncManager m_syncMan;
		private long m_qid;
		private long m_syncerId;

		public SyncTask(IQSyncManager sm, long qid, long syncerId) {
			m_syncMan = sm;
			m_qid = qid;
			m_syncerId = syncerId;
		}

		public void run() {
			QSyncTaskResult result = new QSyncTaskResult();
			result.qid = m_qid;
			result.syncerId = m_syncerId;
			try{
				exec();
				result.status = QSyncTaskStatus.SYNCED;  
			}catch(Exception e){
				result.status = QSyncTaskStatus.ERROR;
				result.errorClass = e.getClass().getName();
				result.errorMessage = e.getMessage();
				
				StringWriter sw = new StringWriter();
				PrintWriter s = new PrintWriter(sw);
				e.printStackTrace(s);
				result.errorStack = sw.toString();
			}
			m_syncMan.onTaskCompleted(result);
			
		}

		abstract void exec();
		
	}
	
	static class InitialSync extends SyncTask{
		IQSyncer m_syncer;
		public InitialSync(IQSyncManager sm, IQSyncer syncer, long qid, long syncerId) {
			super(sm, qid, syncerId);
			m_syncer = syncer;
		}
		public void exec() {
			m_syncer.initialSync();
			
		}
	}

	static class RecordSync extends SyncTask{
		private IQSyncer m_syncer;
		private long m_recordId;

		public RecordSync(IQSyncManager sm, IQSyncer syncer, long qid, long syncerId, long recordId) {
			super(sm,qid,syncerId);
			m_syncer = syncer;
			m_recordId = recordId;
		}

		@Override
		void exec() {
			m_syncer.sync(m_recordId);
		}
	}
	
	private int m_maxNumberOfSyncTasks = DEFAULT_MAX_SYNC_TASKS;
	private int m_maxSyncTaskDurationMs = DEFAULT_MAX_TASK_DURATION;
	private IQSyncManagerExternals m_syncerExternals;
	
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
	private Map<QSKey, IQSyncer> m_syncers = new HashMap<QSKey, IQSyncer>();
//	private Map<Long, QSyncQueueInfo> m_syncTime = new HashMap<Long, QSyncQueueInfo>();
	private Map<Long, QSyncQueueInfo> m_syncTime = new HashMap<Long, QSyncQueueInfo>();
	
	public void setMaxNumberOfSyncTasks(int value) {
		m_maxNumberOfSyncTasks = value;
	}

	public int getMaxNumberOfSyncTasks() {
		return m_maxNumberOfSyncTasks;
	}

	public void setMaxSyncTaskDurationMs(int value) {
		m_maxSyncTaskDurationMs = value;

	}

	public int getMaxSyncTaskDurationMs() {
		return m_maxSyncTaskDurationMs;
	}

	public IQSyncManagerExternals getSyncerExternals() {
		return m_syncerExternals;
	}
	
	public void setSyncerExternals(IQSyncManagerExternals value) {
		m_syncerExternals = value;
	}


	public void registerQueue(long qid, long syncerId) {
		queueBL().registerQueue(qid, syncerId, QSyncTaskStatus.NOT_STARTED);
		IQSyncer syncer = m_syncerExternals.getQSyncer(qid, null);
		syncer.connectToQueue(qid);
		m_syncers.put(new QSKey(qid, syncerId), syncer);

	}

	public void unRegisterQueue(long qid, long syncerId) {
		queueBL().dropQueue(qid);

	}

	public void onEveryMinute() {
		ResSet rs = queueBL().getQueues();
		int nexec = m_maxNumberOfSyncTasks;
		
		while(nexec > 0 && rs.next()){
			long qid = rs.getLong(1);
			long syncerId = rs.getLong(2);
			QSyncTaskStatus status = QSyncTaskStatus.valueOf(rs.getString(3).trim());

			SyncTask task = null; 
			
			if(QSyncTaskStatus.NOT_STARTED.equals(status)){
				task = new InitialSync(this, getSyncer(qid, syncerId), qid, syncerId);

			}

			if(QSyncTaskStatus.SYNCED.equals(status)){
				Long recordId = getQueueRecord(qid);
				if(null != recordId){
					task = new RecordSync(this, getSyncer(qid, syncerId), qid, syncerId, recordId);
				}
			}
			
			if(QSyncTaskStatus.IN_PROCESS.equals(status)){
				nexec --;
			}
			
			if(null != task){
				QSyncQueueInfo sTime = m_syncTime.get(qid);
				if(null == sTime){
					sTime = new QSyncQueueInfo();
					m_syncTime.put(qid, sTime);
				}
				sTime.lastAttempt = ApiStack.getInterface(ITime.class).currentTimeMillis();
				queueBL().updateQueueStatus(task.m_qid, QSyncTaskStatus.IN_PROCESS);
				m_syncerExternals.runTask(task);
				
				nexec --;
			}
		}
		

	}

	private void putQueueRecord(long qid, long recordId){
		IDbQueueFactory.Helper.getQueue(qid).put(recordId);
	}
	
	private Long getQueueRecord(long qid) {
		return (Long) IDbQueueFactory.Helper.getQueue(qid).get(0L);
	}

	private IQSyncer getSyncer(long qid, long syncerId) {
		return m_syncers.get(new QSKey(qid, syncerId));
	}

	public void onRecordChanged(Long qid, Long recordId) {
		putQueueRecord(qid, recordId);
	}

	public void onTaskCompleted(QSyncTaskResult result) {
		QSyncQueueInfo sTime = m_syncTime.get(result.qid);
		if(null == sTime){
			throw new ETaskWasNotStarted(result.qid);
		}
		if(sTime.lastAttempt < Math.max(sTime.lastError, sTime.lastSynced))
			throw new ETaskWasNotStarted(result.qid);
		queueBL().taskCompleted(result);
		
		if(QSyncTaskStatus.SYNCED.equals(result.status))
			sTime.lastSynced = ApiStack.getInterface(ITime.class).currentTimeMillis();
		if(QSyncTaskStatus.ERROR.equals(result.status))
			sTime.lastError = ApiStack.getInterface(ITime.class).currentTimeMillis();

	}

	public QSyncQueueInfo getQueueInfo(long qid) {
		QSyncQueueInfo res = null;
		ResSet rs = queueBL().getQueueInfo(qid);
		if(rs.next()){
			res = new QSyncQueueInfo();
			QSyncQueueInfo sTime = m_syncTime.get(qid);
			if(null != sTime){
				res.lastAttempt = sTime.lastAttempt;
				res.lastError   = sTime.lastError;
				res.lastSynced  = sTime.lastSynced;
			}
			res.result = new QSyncTaskResult();
			res.result.syncerId = rs.getLong(2);
			res.result.status = QSyncTaskStatus.valueOf(QSyncTaskStatus.class, rs.getString(3).trim());
			if(QSyncTaskStatus.ERROR.equals(res.result.status)){
				res.result.errorClass = rs.getString(4);
				res.result.errorMessage = rs.getString(5);
				res.result.errorStack = rs.getString(6);
			}
		}
		return res;
	}

	public List<QSyncQueueInfo> getTopQueuesInfo(long qid, int n,
			EnumSet<QSyncTaskStatus> statusToFilter) {
		// TODO Auto-generated method stub
		return null;
	}


	private TQSyncQueues.BL queueBL(){
		return SrvApiAlgs2.getIServerTran().instantiateBL(TQSyncQueues.BL.class);
	}
}
