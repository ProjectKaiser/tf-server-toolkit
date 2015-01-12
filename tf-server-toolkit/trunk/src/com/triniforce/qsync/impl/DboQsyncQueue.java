/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.dbo.IDBObject;

public class DboQsyncQueue implements IDBObject{

	protected long m_qid;
	private Class m_clsSyncer;

	public DboQsyncQueue(long qid, Class clsSyncer) {
		m_qid = qid;
		m_clsSyncer = clsSyncer;
	}

	public IDBObject[] getDependiencies() {
		return new IDBObject[]{};
	}

	public IDBObject[] synthDBObjects() {
		return new IDBObject[]{};
	}

	public Class getActualizerClass() {
		return DboQSyncActualizer.class;
	}

	public Object getKey() {
		return String.format("%s:%d",m_clsSyncer.getName(), m_qid);
	}

	public long getQueueId() {
		return m_qid;
	}
	
	

}
