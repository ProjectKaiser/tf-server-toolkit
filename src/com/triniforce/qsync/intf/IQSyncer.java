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
    void sync(long qid, long recordId);
    void finit(Throwable t);
}
