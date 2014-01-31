/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.triniforce.utils.TFUtils;

/**
 * Intended as a part of PeriodicalKeyTasks. Executes not more than nThreads, rejects task duplicates (Set.contains())
 *
 */
public class KeyTaskExecutor extends ThreadPoolExecutor{
	
	private final int m_threads;
	static Set<Runnable> m_tasksStaticList = new HashSet<Runnable>();
	private int m_rejectedCount;
	private int m_finishedCount;

    public KeyTaskExecutor(int coreThreads, int nThreads){
        super(coreThreads, nThreads + 10,
                    120L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>()
                    //new LinkedBlockingQueue<Runnable>()
                    
        );
        TFUtils.assertTrue(nThreads >0, "nthreads =" + nThreads);
        m_threads = nThreads;
    }   

	
	public KeyTaskExecutor(int nThreads){
	    this(4, nThreads);
	}
	
	class KeyTask implements Runnable{
	    private final Runnable m_task;
        public KeyTask(Runnable task) {
            m_task = task;
        }
        public void run() {
            synchronized (m_tasksStaticList) {
                if (m_tasksStaticList.contains(m_task) || getActiveCount() > m_threads) {
                    m_rejectedCount++;
                    return;
                }
                //m_tasks.add(m_task);
                m_tasksStaticList.add(m_task);
            }
            try{
                m_task.run();
            }finally{
                synchronized (m_tasksStaticList) {
                    m_tasksStaticList.remove(m_task);
                    m_finishedCount++;
                }
            }
        }
	}
	
	@Override
	public void execute(final Runnable task) {
		super.execute(new KeyTask(task));
	}
	
	public Future submit(final Runnable task) {
	    return super.submit(new KeyTask(task));
	}
	
	public int getRejectedCount() {
		return m_rejectedCount;
	}

	public int getFinishedCount() {
		return m_finishedCount;
	}
	
}
