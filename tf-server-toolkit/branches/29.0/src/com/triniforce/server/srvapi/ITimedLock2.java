/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.srvapi;

public interface ITimedLock2 {
    public interface ITimedLockCB{
        /**
         * Invoked when lock is timed out
         */
        void unlocked();
    }
    
    public long getTimeout();
    public void setTimeout(long value);
    /**
     * Acquires the lock, wait if needed
     */
    public void acquireLock(ITimedLockCB cb);
    
    public Thread getLockerThread();
    
    /**
     * Value from acquireLock should be used. If curent cb does not equal to this cb niothing happens
     */
    public void releaseLock(ITimedLockCB cb);
    /**
     * If lock is timed out it is forcebly unlocked and cb is invoked
     */
    public void checkTimeout();
    
    /**
     * @return null of none
     */
    public ITimedLockCB getCurrentCB();
    
    boolean isAvailable();
    
    long getCurrentTimestamp();
}
