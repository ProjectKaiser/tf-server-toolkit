/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

/**
 * User of IQSyncManager must provide some function explained in this interface
 * 
 */
public interface IQSyncManagerExternals {
    IQSyncer getQSyncer(long qid, Class<IQSyncer> syncClass);
    /**
     * Execute runnable asynchronously
     * @param r
     */
    void runTask(Runnable r);
}
