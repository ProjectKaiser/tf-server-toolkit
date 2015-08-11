/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

import java.util.EnumSet;
import java.util.List;

import com.triniforce.server.plugins.kernel.SrvSmartTran;

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
     * Default is 4
     * @param value
     */
    void setMaxNumberOfInitTasks(int value);
    int getMaxNumberOfInitTasks();

    
    /**
     * Default is 60000
     * @param value
     */
    void setMaxIncrementalSyncTaskDurationMs(int value);
    int getMaxIncrementalSyncTaskDurationMs();
    
    IQSyncManagerExternals getSyncerExternals();
    
    
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
    void registerQueue(long qid, long syncerId, IQSyncer qSyncer);

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
     * <li>Process errors
     * <li>Check pending db queues?
     */
    void onEveryMinute();
    void onTaskCompleted(QSyncTaskResult result);
    
    /**
     * Called by {@link SrvSmartTran} when transaction is finished. Must ignore not registered queues.
     * 
     * @param qid Queue id
     * @return false if queue is not registered
     */
    boolean onQueueChanged(Long qid);

    /*
     * 
     * Status methods
     * 
     */
    
    
    QSyncQueueInfo getQueueInfo(long qid);
    
    /**
     * @return list of n queues sorted by lastAttempt()
     */
    List<QSyncQueueInfo> getTopQueuesInfo(int n, EnumSet<QSyncTaskStatus> statusToFilter);
    
}
