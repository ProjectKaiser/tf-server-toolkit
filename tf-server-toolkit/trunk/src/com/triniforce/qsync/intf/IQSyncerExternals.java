/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

public interface IQSyncerExternals {
    IQSyncer newQSyncerInstance(Long syncerId);
    void runTask(Runnable r);
}
