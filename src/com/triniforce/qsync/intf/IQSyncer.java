/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

/**
 * sync is called multiple times, if error occurs or timeout expired {@link #finit()} is called
 */
public interface IQSyncer {
    /**
     * Called once 
     */
    void initialSync();
    
    /**
     * Called every time syncer is connected to queue (normally once per application live time) 
     */
    void connectToQueue(long qid);
    
    /**
     * Called in the beginnig of sync cycle
     */
    void init();
    
    /**
     * Called for every record in queue
     */
    void sync(Object o);

    /**
     * Called in the end of sync cycle 
     */
    void finit(Throwable t);
}
