/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.concurrent;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.triniforce.utils.ITime;

public class ScheduledExecutorTask implements ScheduledFuture<Object>, Runnable {

    long m_nextStartMs = -1;
    volatile boolean m_cancelled = false;
    boolean m_mayInterruptIfRunning = false;
    private final Runnable m_runnableTask;
    private final long m_initialDelayMs;
    private final long m_delayMs;
    
    private ITime m_time;
	private boolean m_done;

    @Override
    public String toString() {
    	String rName = m_runnableTask == null? "" : m_runnableTask.getClass().getName();
        return getClass().getSimpleName() +": " + rName +": "+ m_initialDelayMs + "," + m_delayMs + ", " + m_cancelled;
    }
    
    public ScheduledExecutorTask(Runnable r, long initialDelayMs, long delayMs) {
        m_runnableTask = r;
        m_initialDelayMs = initialDelayMs;
        m_delayMs = delayMs;
        
        setTime(new ITime(){
            @Override
            public long currentTimeMillis() {
                return System.currentTimeMillis();
            }
        });
        
    }

    /**
     * @return false if task must not be run anymore
     */
    synchronized public boolean calcNextStart() {
        if(m_cancelled || m_done){
            return false;
        }
        if (m_nextStartMs < 0) {
            m_nextStartMs = getTime().currentTimeMillis() + m_initialDelayMs;
            return true;
        } else {
            if(m_delayMs <= 0){
            	m_done = true;
                return false;
            }
            m_nextStartMs = getTime().currentTimeMillis() + m_delayMs;
            return true;
        }
    }

    @Override
    public long getDelay(TimeUnit paramTimeUnit) {
        return paramTimeUnit.convert(
                m_nextStartMs - getTime().currentTimeMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed otherD) {
        Long msOther = otherD.getDelay(TimeUnit.MILLISECONDS);
        Long msMe = getDelay(TimeUnit.MILLISECONDS);
        return msMe.compareTo(msOther);
    }

    @Override
    synchronized public boolean cancel(boolean mayInterruptIfRunning) {
        m_cancelled = true;
        m_mayInterruptIfRunning = mayInterruptIfRunning;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return m_cancelled;
    }

    @Override
    public boolean isDone() {
        return m_done || m_cancelled;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long paramLong, TimeUnit paramTimeUnit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    public Runnable getRunnableTask() {
        return m_runnableTask;
    }

    public ITime getTime() {
        return m_time;
    }

    public void setTime(ITime time) {
        m_time = time;
    }

    @Override
    public void run() {
        if(null == m_runnableTask){
            return;
        }
        m_runnableTask.run();
    }

}
