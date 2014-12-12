/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

public interface IQSyncTask {
    long getQueueId();
    long getSyncerId();
    long lastAttempt();
    Throwable getException();
    
}
