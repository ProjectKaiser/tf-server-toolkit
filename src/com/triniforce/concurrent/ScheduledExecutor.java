/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class ScheduledExecutor extends ThreadPoolExecutor implements ScheduledExecutorService, Runnable{
    
    abstract class Cmd implements Runnable{
               
    }
    
    class CmdScheduleTask extends Cmd{
        private final ScheduledExecutorTask m_task;

        CmdScheduleTask(ScheduledExecutorTask task){
            m_task = task;
        }

        public ScheduledExecutorTask getTask() {
            return m_task;
        }

        @Override
        public void run() {
            m_tasksQueue.add(m_task);
        }
    }
    
    LinkedBlockingQueue<Cmd> m_CommandQueue = new LinkedBlockingQueue<Cmd>();
    PriorityQueue<ScheduledExecutorTask> m_tasksQueue = new PriorityQueue<ScheduledExecutorTask>(); 
    
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
        submit(this);
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
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable r,
            long initialDelay, long delay, TimeUnit paramTimeUnit) {
        long initialDelayMs = paramTimeUnit.convert(initialDelay, TimeUnit.MILLISECONDS);
        long delayMs = paramTimeUnit.convert(delay, TimeUnit.MILLISECONDS);
        ScheduledExecutorTask t = new ScheduledExecutorTask(r, initialDelayMs, delayMs);
        m_CommandQueue.offer(new CmdScheduleTask(t));
        return t;
    }

    @Override
    public void run() {
        while(true){
            try {
                ScheduledExecutorTask t = m_tasksQueue.poll();
                long delayMs;
                if( null != t){
                    delayMs = t.getDelay(TimeUnit.MILLISECONDS);
                }else{
                    delayMs = 60 * 1000;
                }
                Cmd c = m_CommandQueue.poll(delayMs, TimeUnit.MILLISECONDS);
                if(null != c){
                    c.run();
                }
            } catch (InterruptedException e) {
                break;
            }
            
        }
        
    }    

}
