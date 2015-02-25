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

import com.triniforce.utils.ITime.ITimeHelper;

public class ScheduledExecutorTask implements ScheduledFuture {

    long m_nextStartMs = -1;
    boolean m_cancelled = false;
    boolean m_mayInterruptIfRunning = false;
    private final Runnable m_runnableTask;
    private final long m_initialDelayMs;
    private final long m_delayMs;

    public ScheduledExecutorTask(Runnable r, long initialDelayMs, long delayMs) {
        m_runnableTask = r;
        m_initialDelayMs = initialDelayMs;
        m_delayMs = delayMs;
        calcNextStart();
    }

    /**
     * @return false if task must not be run anymore
     */
    public boolean calcNextStart() {
        if(m_cancelled){
            return false;
        }
        if (m_nextStartMs < 0) {
            m_nextStartMs = ITimeHelper.currentTimeMillis() + m_initialDelayMs;
            return true;
        } else {
            if(m_delayMs <= 0){
                return false;
            }
            m_nextStartMs = ITimeHelper.currentTimeMillis() + m_delayMs;
            return true;
        }
    }

    @Override
    public long getDelay(TimeUnit paramTimeUnit) {
        return paramTimeUnit.convert(
                m_nextStartMs - ITimeHelper.currentTimeMillis(),
                TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed otherD) {
        Long msOther = otherD.getDelay(TimeUnit.MILLISECONDS);
        Long msMe = getDelay(TimeUnit.MILLISECONDS);
        return msMe.compareTo(msOther);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
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
        return m_cancelled;
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

}
