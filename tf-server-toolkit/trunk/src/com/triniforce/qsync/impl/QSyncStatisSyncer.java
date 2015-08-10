/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.qsync.intf.IQSyncer;

public abstract class QSyncStatisSyncer{

	protected Long m_qid;

	/**
	 * Used if
	 */
	public QSyncStatisSyncer(long qid) {
		m_qid = qid;
	}
	
	public QSyncStatisSyncer() {
	}
	
	public Long getQueueId() {
		return m_qid;
	}
	
	/**
	 * If getQueueId() is null will be generated 
	 */
	public void setQueueId(long value) {
		m_qid = value;
	}

	public abstract IQSyncer createSyncer();

}
