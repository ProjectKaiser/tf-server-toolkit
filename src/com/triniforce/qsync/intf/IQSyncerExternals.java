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
public interface IQSyncerExternals {
    IQSyncer getQSyncer(long qid, long syncerId);
    /**
     * Execute runnable asynchronously
     * @param r
     */
    void runTask(Runnable r);
}
