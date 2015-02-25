/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ScheduledExecutor extends ThreadPoolExecutor implements ScheduledExecutorService{
    
    public static class MyThreadFactory implements ThreadFactory{

        
        AtomicInteger cnt = new AtomicInteger(0); 
        
        @Override
        public Thread newThread(Runnable paramRunnable) {
            int curCnt = cnt.incrementAndGet();
            Thread t = new Thread(paramRunnable, "ScheduledExecutor_" + curCnt);
            return t;
        }
    }
    
    public ScheduledExecutor(int corePoolSize, int maxmimumPoolSize) {
        super(corePoolSize + 1, maxmimumPoolSize + 1, 60, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(), new MyThreadFactory());
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable paramRunnable, long paramLong,
            TimeUnit paramTimeUnit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> paramCallable,
            long paramLong, TimeUnit paramTimeUnit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable paramRunnable,
            long paramLong1, long paramLong2, TimeUnit paramTimeUnit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable paramRunnable,
            long paramLong1, long paramLong2, TimeUnit paramTimeUnit) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    

}
