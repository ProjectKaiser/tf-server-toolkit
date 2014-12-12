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
     * Configuration methods
     * 
     * 
     */
    
    
    /**
     * Affects sync process after some time
     */
    void setMaxNumberOfSyncTasks(int value);
    int getMaxNumberOfSyncTasks();
    
    /**
     * Affects sync process after some time
     */
    void setQSyncerExternals(IQSyncerExternals factory);
    IQSyncerExternals getSyncerExternals();
    
    
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
    void unRegisterQueue(long qid);
    
    /*
     * 
     * Events
     * 
     * 
     */
    
    /**
     * 
     * Timer event
     * 
     */
    void onEveryMinute();
    void onRecordChanged(Long qid, Long id);
    void onTaskCompleted(IQSyncTask task);

    /*
     * 
     * Status methods
     * 
     */
    
    QSyncQueueInfo getQueueInfo(long qid, long syncerId);
    
    /**
     * @return list of n queues sorted by lastAttempt() DESC
     */
    List<QSyncQueueInfo> getTopQueuesInfo(long qid, int n, EnumSet<QSyncTaskResult> resultToFilterBy);
    
}
