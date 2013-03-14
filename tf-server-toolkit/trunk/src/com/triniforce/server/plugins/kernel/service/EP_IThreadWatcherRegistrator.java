/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.service;

import java.util.Map;

public interface EP_IThreadWatcherRegistrator {
    public void registerThread(Thread thread, String threadExtraInfo);
    public void unregisterThread(Thread thread);
    public void registerLongTermOp(Thread thread);
    public void unregisterLongTermOp(Thread thread);
    
    public boolean isAnyShortThreadWaiting(long thresholdMilliseconds);
    public Map<Thread, ThreadInfo> getWaitingThreads(long thresholdMilliseconds);
    
    long getThreshold();
    void setThreshold(long thresholdMilliseconds);
    
    public ThreadInfo queryThreadInfo(Thread thread);
    
    public static class ThreadInfo{
        private String m_threadName;
        private String m_threadExtraInfo;
        private Long m_watchStart;
        private boolean m_isLongOp;
        private Thread m_thread;
        private int m_lockCnt;
        public void setThreadName(String threadName) {
            m_threadName = threadName;
        }
        public String getThreadName() {
            return m_threadName;
        }
        public void setThreadExtraInfo(String threadExtraInfo) {
            m_threadExtraInfo = threadExtraInfo;
        }
        public String getThreadExtraInfo() {
            return m_threadExtraInfo;
        }
        public void setWatchStart(Long watchStart) {
            m_watchStart = watchStart;
        }
        public Long getWatchStart() {
            return m_watchStart;
        }
        public void setLongOp(boolean isLongOp) {
            m_isLongOp = isLongOp;
        }
        public boolean isLongOp() {
            return m_isLongOp;
        }
        public void setThread(Thread thread) {
            m_thread = thread;
        }
        public Thread getThread() {
            return m_thread;
        }
        public void setLockCnt(int lockCnt) {
            m_lockCnt = lockCnt;
        }
        public int getLockCnt() {
            return m_lockCnt;
        }
    }
    
}
