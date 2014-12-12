/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

import com.triniforce.utils.StringSerializer;

public class QSyncQueueInfo {

    private QSyncTaskResult m_result;
    /**
     * Makes sense only of result is ERROR
     */
    private String m_errorMessage;
    
    private long m_lastSuccess;
    private long m_lastFailure;
    
    private long m_qid;
    private long m_syncerId;

    public QSyncTaskResult getResult() {
        return m_result;
    }

    public void setResult(QSyncTaskResult result) {
        m_result = result;
    }

    public String getErrorMessage() {
        return m_errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        m_errorMessage = errorMessage;
    }

    /**
     * @return Either m_lastSuccess or m_lastFailer, what is more
     */
    public long getLastAttempt() {
        return m_lastFailure > m_lastSuccess ? m_lastFailure : m_lastSuccess;
    }

    public long getLastSuccess() {
        return m_lastSuccess;
    }

    public void setLastSuccess(long lastSuccess) {
        m_lastSuccess = lastSuccess;
    }

    public long getLastFailure() {
        return m_lastFailure;
    }

    public void setLastFailure(long lastFailure) {
        m_lastFailure = lastFailure;
    }

    public long getQid() {
        return m_qid;
    }

    public void setQid(long qid) {
        m_qid = qid;
    }

    public long getSyncerId() {
        return m_syncerId;
    }

    public void setSyncerId(long syncerId) {
        m_syncerId = syncerId;
    }
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return StringSerializer.Object2JSON(this);
    }
    
}
