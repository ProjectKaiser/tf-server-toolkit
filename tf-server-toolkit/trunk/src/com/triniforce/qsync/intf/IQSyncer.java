/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

public interface IQSyncer {
    /**
     * @param qid Queue Id
     * @return true if sync completed
     */
    boolean sync(long qid);
}
