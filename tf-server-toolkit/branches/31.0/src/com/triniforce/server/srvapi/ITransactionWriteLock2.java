/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.srvapi;

public interface ITransactionWriteLock2 {
    public void lock();
    public void lock(ITimedLock2.ITimedLockCB cb);
    public void unlock();
}
