/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;
import java.text.MessageFormat;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.srvapi.ITimedLock2;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;
import com.triniforce.utils.Utils;

public class TimedLock2 implements ITimedLock2, IPKEPAPI{

    protected Semaphore m_s = new Semaphore(1);
    protected long m_timeout = 10000;
    protected long m_timestamp;
    protected String m_lockerThreadName;
    ITimedLock2.ITimedLockCB m_cb;    
    ITimedLockCB m_dummy = new ITimedLockCB(){
        public void unlocked() {
        }};
    
    
    protected Thread m_lockerThread;
        
    protected void acquireWithLog(){
        try {
            while (!m_s.tryAcquire(2000, TimeUnit.MILLISECONDS)) {
                String s = MessageFormat.format("TimedLock: Thread {0} waits for {1}", Thread.currentThread(), getLockerThread());
                ApiAlgs.getLog(this).trace(s);
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }
    
    public void acquireLock(ITimedLockCB cb) {
        if(Utils.equals(m_cb, cb))return;
        if(Utils.equals(m_lockerThread, Thread.currentThread())) return;
        try {
            acquireWithLog();
            m_cb = cb;
            ITime tm = ApiStack.getInterface(ITime.class);
            m_timestamp = tm.currentTimeMillis();
            m_lockerThreadName = Thread.currentThread().getName();
            m_lockerThread = Thread.currentThread();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

    synchronized public void checkTimeout() {
        if( null == m_cb) return;
        ITime tm = ApiStack.getInterface(ITime.class);
        long cur = tm.currentTimeMillis();
        if( cur - m_timestamp > m_timeout){
            m_cb.unlocked();
            releaseLock(m_dummy);
        }
    }

    public long getTimeout() {
        return m_timeout;
    }
    
    synchronized public void releaseLock(ITimedLockCB cb){
        if(Utils.equals(cb, m_cb) || m_dummy.equals(cb)){
            m_cb = null;
            m_timestamp = 0;
            m_lockerThread = null;
            m_lockerThreadName = null;            
            m_s.release();
        }
    }
    
    public void setTimeout(long value) {
        m_timeout = value;
        
    }
    
    public ITimedLockCB getCurrentCB() {
        return m_cb;
    }

    public boolean isAvailable() {
        return m_s.availablePermits() > 0;
    }

    public long getCurrentTimestamp() {
        return m_timestamp;
    }

    public Thread getLockerThread() {
        return m_lockerThread;
    }

    public Class getImplementedInterface() {
        return ITimedLock2.class;
    }
}
