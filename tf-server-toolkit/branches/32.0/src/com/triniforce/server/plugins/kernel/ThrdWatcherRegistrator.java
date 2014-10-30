/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.util.HashMap;
import java.util.Map;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ITime;
import com.triniforce.utils.TFUtils;


public class ThrdWatcherRegistrator implements IThrdWatcherRegistrator, IPKEPAPI {
    
    static class MyTime implements ITime{
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    }

    private long m_threshold = 1000 * 30;
    
    private ITime m_ITime = new MyTime();
    
    private Map<Thread, ThreadInfo> m_threadMap = new HashMap<Thread, ThreadInfo>();
    
    public synchronized Map<Thread, ThreadInfo> getWaitingThreads(long millisecondsAmount){
        Map<Thread, ThreadInfo> res = new HashMap<Thread, ThreadInfo>();
        long curTime = m_ITime.currentTimeMillis();
        for(ThreadInfo ti:m_threadMap.values()){
            if(curTime - ti.getWatchStart() > millisecondsAmount){
                res.put(ti.getThread(),ti);
            }
        }
        return res;
    }

    public synchronized boolean isAnyShortThreadWaiting(long millisecondsAmount){
        ITime tm = m_ITime;
        long curTime = tm.currentTimeMillis();        
        for(ThreadInfo ti:m_threadMap.values()){
            long intv = curTime - ti.getWatchStart();
            if( (!ti.isLongOp()) &&  (intv > millisecondsAmount) ){
                ApiAlgs.getLog(this).trace("Thread " + ti.getThreadName() + " waiting for " + intv);
                return true;
            }
        }
        return false;
    }

    public ThreadInfo queryThreadInfo(Thread thread){
        ThreadInfo ti = m_threadMap.get(thread);
        return ti;
    }
    
    public synchronized void registerLongTermOp(Thread thread) {
    	ThreadInfo ti = queryThreadInfo(thread);
        TFUtils.assertNotNull(ti, "Thread not registered");
        ti.setLongOp(true);
    }

    public synchronized void registerThread(Thread thread, String threadExtraInfo) {
        ThreadInfo ti = m_threadMap.get(thread);
        if(null == ti){ 
            ti = new ThreadInfo();
            ti.setThreadExtraInfo(threadExtraInfo);
            ti.setLongOp(false);
            ti.setWatchStart(m_ITime.currentTimeMillis());
            ti.setThreadName(thread.getName());
            ti.setThread(thread);
            m_threadMap.put(thread, ti);
        }
        ti.setLockCnt(ti.getLockCnt() + 1);
    }

    public synchronized void unregisterLongTermOp(Thread thread) {
        ThreadInfo ti = queryThreadInfo(thread);
        TFUtils.assertNotNull(ti, "Thread is not registered");
        ti.setLongOp(false);
        ti.setWatchStart(m_ITime.currentTimeMillis()); 
    }

    public synchronized void unregisterThread(Thread thread) {
        if(null == thread) return;
        ThreadInfo ti  = m_threadMap.get(thread);
        if(null == ti) return;
        ti.setLockCnt(ti.getLockCnt() - 1);
        if( 0 == ti.getLockCnt()){
            m_threadMap.remove(thread);
        }
    }

    public long getThreshold() {
        return m_threshold;
    }

    public void setThreshold(long thresholdMilliseconds) {
        m_threshold = thresholdMilliseconds;
    }

    public void setITime(ITime time) {
        m_ITime = time;
    }

    public ITime getITime() {
        return m_ITime;
    }

    public Class getImplementedInterface(){
        return IThrdWatcherRegistrator.class;
    }

}
