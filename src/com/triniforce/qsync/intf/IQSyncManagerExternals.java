/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

import com.triniforce.server.srvapi.BasicServerTask;

/**
 * User of IQSyncManager must provide some function explained in this interface
 * 
 */
public interface IQSyncManagerExternals {
    
    /**
     * 
     * @param qid
     * @param syncerId
     * @return null if syncer is not registered
     */
    IQSyncer getQSyncer(long qid, Long syncerId);

    /**
     * Execute runnable asynchronously. E.g. using {@link BasicServerTask}
     * @param r
     */
    //TODO remove
    void runTask(Runnable r);
    //TODO
    //void runSync(Runnable r);
    //void runInitialSync(Runnable r);
}
