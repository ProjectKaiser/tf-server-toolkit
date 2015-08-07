/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.dbo.IDBObject;
import com.triniforce.qsync.intf.IQSyncer;

public abstract class DboQSyncQueue implements IDBObject{

	protected Long m_qid;

	public DboQSyncQueue(long qid) {
		m_qid = qid;
	}
	
	public DboQSyncQueue() {
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
		return String.format("%s:%d",this.getClass().getName(), m_qid);
	}

	public Long getQueueId() {
		return m_qid;
	}
	
	public void setQueueId(long value) {
		m_qid = value;
	}

	public abstract IQSyncer createSyncer();

}
