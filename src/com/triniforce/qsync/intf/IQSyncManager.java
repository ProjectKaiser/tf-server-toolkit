/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

import java.util.EnumSet;
import java.util.List;

public interface IQSyncManager {
    
    /*
     *
     * Configuration methods. Most of them affects sync process after some time.
     * 
     * 
     */
    
    
    /**
     * Default is 10
     * @param value
     */
    void setMaxNumberOfSyncTasks(int value);
    int getMaxNumberOfSyncTasks();
    
    /**
     * Default is 60000
     * @param value
     */
    void setMaxSyncSessionDurationMs(int value);
    int getMaxSyncSessionDurationMs();
    
    IQSyncerManagerExternals getSyncerExternals();
    
    
    /* 
     * 
     * Operation methods
     * 
     * 
     */
    
    /**
     * Note that qid and syncerId are PRIMARY KEY
     * @param qid
     * @param syncerId
     */
    void registerQueue(long qid, long syncerId);

    /**
     * ?? What if tasks are in process?
     * @param qid
     */
    void unRegisterQueue(long qid, long syncerId);
    
    /*
     * 
     * Events
     * 
     * 
     */
    
    /**
     * Timer event
     */
    void onEveryMinute();
    void onRecordChanged(Long qid, Long recordId);
    void onTaskCompleted(QSyncTaskResult result);

    /*
     * 
     * Status methods
     * 
     */
    
    QSyncQueueInfo getQueueInfo(long qid, long syncerId);
    
    /**
     * @return list of n queues sorted by lastAttempt() DESC
     */
    List<QSyncQueueInfo> getTopQueuesInfo(long qid, int n, EnumSet<QSyncTaskStatus> statusToFilter);
    
}
