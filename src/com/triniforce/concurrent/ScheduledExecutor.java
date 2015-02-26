/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.triniforce.utils.ICheckInterrupted;


public class ScheduledExecutor extends ThreadPoolExecutor implements ScheduledExecutorService, Runnable{
    
    abstract class Cmd implements Runnable{
               
    }
    
    class CmdScheduleTask extends Cmd{
        private final ScheduledExecutorTask m_task;

        CmdScheduleTask(ScheduledExecutorTask task){
            m_task = task;
        }

        @Override
        public void run() {
        	if(null != m_task && m_task.calcNextStart()){
        		m_taskQueue.add(m_task);
        	}
        }
    }
    
	class TaskWrapper implements Runnable {
		private final ScheduledExecutorTask m_t;

		public TaskWrapper(ScheduledExecutorTask t) {
			m_t = t;
		}

		@Override
		public void run() {
			try {
				
				m_t.run();
				
				//if exception occurs task won't be rescheduled
				
				Cmd c = new CmdScheduleTask(m_t);
				m_commandQueue.add(c);
				
			} catch (RuntimeException r) {
				if(!ICheckInterrupted.Helper.isInterruptedException(r)){
					System.err.println(m_t.toString());
					r.printStackTrace();
				}
			}
		}
	}
    
    BlockingQueue<Cmd> m_commandQueue = new LinkedBlockingQueue<Cmd>();
    Queue<ScheduledExecutorTask> m_taskQueue = new PriorityQueue<ScheduledExecutorTask>();
	private final Future<?> m_schedulerFuture; 
    
    public static class MyThreadFactory implements ThreadFactory{

        AtomicInteger cnt = new AtomicInteger(0); 
        
        @Override
        public Thread newThread(Runnable paramRunnable) {
            int curCnt = cnt.incrementAndGet();
            String sfx = 1 == curCnt? "scheduler": "" + curCnt;
            Thread t = new Thread(paramRunnable, "ScheduledExecutor_" + sfx);
            return t;
        }
    }
    
    public ScheduledExecutor(int corePoolSize, int maxmimumPoolSize) {
        super(corePoolSize + 1, maxmimumPoolSize + 1, 60, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new MyThreadFactory());
        m_schedulerFuture = submit(this);
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
        m_commandQueue.offer(new CmdScheduleTask(t));
        return t;
    }

    public static final int EMPTY_TASK_QUEUE_TIMEOUT_MS = 1000 * 60;
    public static final int REJECTED_EXECUTION_TIMOUT_MS = 50;
    
    @Override
    public void run() {
        while(true){
            try {
                ScheduledExecutorTask t = m_taskQueue.poll();
				long delayMs = null == t ? EMPTY_TASK_QUEUE_TIMEOUT_MS : t.getDelay(TimeUnit.MILLISECONDS); 
                if(delayMs > 0){
                	Cmd c = m_commandQueue.poll(delayMs, TimeUnit.MILLISECONDS);
                    if(null != c){
                    	//put task back
                    	if(null != t){
                    		m_taskQueue.add(t);
                    	}
                        c.run();
                        continue;
                    }
                }
                if(null == t){
                	continue;
                }
                TaskWrapper tw = new  TaskWrapper(t);
                try{
                	submit(tw);
                }catch(RejectedExecutionException re){
                	//put task back
                	m_taskQueue.add(t);
                	Thread.sleep(REJECTED_EXECUTION_TIMOUT_MS);
                }
            } catch (InterruptedException e) {
                break;
            } catch (RuntimeException r){
            	r.printStackTrace();
            	ICheckInterrupted.Helper.sleep(1000);
            }
        }
        
    }

	public Future<?> getSchedulerFuture() {
		return m_schedulerFuture;
	}    
}
