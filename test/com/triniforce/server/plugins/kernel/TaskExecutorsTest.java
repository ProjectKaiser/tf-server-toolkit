/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.server.srvapi.ITaskExecutors.ExecutorKey;
import com.triniforce.server.srvapi.ITaskExecutors.LongTaskExecutorKey;
import com.triniforce.server.srvapi.ITaskExecutors.PeriodicalTaskExecutorKey;
import com.triniforce.server.srvapi.ITaskExecutors.ShortTaskExecutorKey;
import com.triniforce.server.srvapi.InitFinitTask;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ICheckInterrupted;

public class TaskExecutorsTest extends TFTestCase {
    
    public void testNormalTasks() throws InterruptedException, ExecutionException {
        
        //run a lot of tasks which are waiting for lock and then starts
        
        ITaskExecutors te = new TaskExecutors();

        List<InitFinitTask> tasks = new ArrayList<InitFinitTask>();

        final Lock lock = new ReentrantLock();
        lock.lock();

        //LATER: must be MAX_NORMAL_THREADS * 10
        for (int i = 0; i < TaskExecutors.MAX_NORMAL_THREADS; i++) {

            final Integer cnt = new Integer(i);
            InitFinitTask task = new InitFinitTask() {

                public void run() {
                    trace("trying...:" + cnt);
                    lock.lock();
                    lock.unlock();
                    trace("ok:" + cnt);
                }
            };
            tasks.add(task);
        }
        List<Future> futures = new ArrayList<Future>();
        for(InitFinitTask task: tasks){
            futures.add(te.execute(ITaskExecutors.normalTaskExecutorKey, task));            
        }
        ICheckInterrupted.Helper.sleep(1000);
        lock.unlock();
        for(Future f: futures){
            f.get();
        }
        te.shutdownNow();
        te.awatTermination(10000);

    }
        
        

    public void testKeys(){
        assertEquals(new LongTaskExecutorKey(), ITaskExecutors.longTaskExecutorKey);
        assertEquals(new ShortTaskExecutorKey(), ITaskExecutors.shortTaskExecutorKey);
        assertEquals(new PeriodicalTaskExecutorKey(), ITaskExecutors.periodicalTaskExecutorKey);
        assertFalse(new ITaskExecutors.LongTaskExecutorKey().equals(new ITaskExecutors.PeriodicalTaskExecutorKey()));
        Set<ExecutorKey> keys = new HashSet<ExecutorKey>();
        keys.add(new LongTaskExecutorKey());
        assertTrue(keys.contains(new LongTaskExecutorKey()));
        
    }
    
    Integer nTerminated = 0;
    
    @Override
    public void test() throws Exception {
        ITaskExecutors te = new TaskExecutors();
        assertTrue(te.executorKeys().contains(new LongTaskExecutorKey()));
        assertTrue(te.executorKeys().contains(new ShortTaskExecutorKey()));
        assertTrue(te.executorKeys().contains(new ITaskExecutors.NormalTaskExecutorKey()));
        
        final SynchronousQueue<String> q1 = new SynchronousQueue();
        
        nTerminated = 0;
        
        InitFinitTask if1 = new InitFinitTask(){
            public void run() {
                try {
                    try{
                        q1.take();
                        q1.put("");
                        Thread.sleep(10000);
                    }finally{
                        synchronized(nTerminated){
                            nTerminated ++;
                        }
                        trace("Thread terminated");
                    }
                } catch (InterruptedException e) {
                    trace(e);
                }
            };
        };
        
        te.execute(ITaskExecutors.shortTaskExecutorKey, if1);
        assertEquals(1, te.getTasksCount());
        assertEquals(0, te.getCompletedTasksCount());
        q1.put("");
        q1.take();
        
        InitFinitTask if2 = new InitFinitTask(){
            public void run() {
                try {
                    try{
                        q1.take();
                        q1.put("");
                        Thread.sleep(10000);
                    }finally{
                        synchronized(nTerminated){
                            nTerminated ++;
                        }                        
                        trace("Thread terminated");
                    }
                } catch (InterruptedException e) {
                    trace(e);
                }                
            };
        };
        
        te.execute(ITaskExecutors.longTaskExecutorKey, if2);
        
        q1.put("");
        q1.take();
        
        assertEquals(2, te.getTasksCount());
        assertEquals(0, te.getCompletedTasksCount());
        
        te.shutdownNow();
        te.awatTermination(1000);
        for(ExecutorKey key: te.executorKeys()){
        	ExecutorService e = te.getExecutor(key);
            assertTrue(e.isTerminated());
        }
        assertEquals(2, te.getTasksCount());
        assertEquals((Integer)2, nTerminated);
        //sometimes does not work
        //assertEquals(2, te.getCompletedTasksCount());
    }
    
    public static class NamedTestTask extends InitFinitTask {
        private final String m_dsGlobalId;
        private final SynchronousQueue m_q;

        @Override
        public String toString() {
            return m_dsGlobalId + "_" + super.toString();
        }

        public NamedTestTask(String dsGlobalId, boolean isInitial, SynchronousQueue q) {
            m_dsGlobalId = dsGlobalId;
            m_q = q;
        }


        public void run(){
            try {
                m_q.take();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((m_dsGlobalId == null) ? 0 : m_dsGlobalId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NamedTestTask other = (NamedTestTask) obj;
            if (m_dsGlobalId == null) {
                if (other.m_dsGlobalId != null)
                    return false;
            } else if (!m_dsGlobalId.equals(other.m_dsGlobalId))
                return false;
            return true;
        }

    }

    public void testBugFewTasksWithSameKey() throws InterruptedException{
        
        TaskExecutors te = new TaskExecutors();
        
        final SynchronousQueue q = new SynchronousQueue();
        
        NamedTestTask r1 = new NamedTestTask("l1", true, q);
        NamedTestTask r2 = new NamedTestTask("l1", true, q);
        
        assertEquals(0, te.getCompletedTasksCount());
        te.execute(ITaskExecutors.longTaskExecutorKey, r1);
        te.execute(ITaskExecutors.longTaskExecutorKey, r2);
        
        //r2 must be rejected
        while(te.getCompletedTasksCount() == 0){
            ICheckInterrupted.Helper.sleep(1000);
        }
        
        q.put("");
        te.shutdownNow();
        te.awatTermination(10000);
        
        
    }
    
    static int executed = 0;
    static Exception ERROR = null;
    
    static Object SYNCH = new Object();
    
    static class T1 extends InitFinitTask{
		@Override
		public void run() {
			ApiAlgs.getLog(this).trace("started...");
			executed ++;
			
				synchronized (SYNCH) {
					ApiAlgs.getLog(this).trace("synching");
					SYNCH.notify();
					try {
						SYNCH.wait();
					} catch (InterruptedException e) {
						ApiAlgs.getLog(this).error("", e);
						ERROR = e;
					}
				}
			ApiAlgs.getLog(this).trace("ended...");
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj.getClass().equals(T1.class);
			
		}
		
		@Override
		public int hashCode() {
			return 1;
		}
    }
    
    public void testSubmitEqualTasks() throws InterruptedException{
        TaskExecutors te = new TaskExecutors();
        int cntCompleted = te.getCompletedTasksCount();
        int cnt = te.getTasksCount();
        synchronized (SYNCH) {
        	trace("start task 1");
	        te.execute(TaskExecutors.longTaskExecutorKey, new T1());
	        SYNCH.wait();
	        trace("start task 2");
	        te.execute(TaskExecutors.longTaskExecutorKey, new T1());
	        trace("waiting for SYNCH");
	        
	        while(cnt + 2 < te.getTasksCount()) {
	        	Thread.sleep(100); // Both task should be started
	        }
	        int iterMax = 100;
	        while(cntCompleted + 1 != te.getCompletedTasksCount() && iterMax > 0){
	        	// Second task should be completed before first ended
	        	Thread.sleep(10L);
	        	iterMax --;
	        } 
	        
	        trace("waiting for NOTIFIING");
	        SYNCH.notify();
		}
        
        trace("shutdown process");
        te.shutdownNow();
        te.awatTermination(1000L);
        
        assertNull(""+ERROR, ERROR);
        assertEquals("" + executed, 1, executed);
    	
    }
    
    /*FAILED TRACE
     * 
     * TaskExecutorsTest 2021-08-26 18:45:58.446 DEBUG com.triniforce.server.plugins.kernel.TaskExecutorsTest  - start task 1
TaskExecutorsTest 2021-08-26 18:45:58.447 DEBUG com.triniforce.server.plugins.kernel.TaskExecutorsTest  - start task 2
TaskExecutorsTest 2021-08-26 18:45:58.447 DEBUG com.triniforce.server.plugins.kernel.TaskExecutorsTest  - waiting for SYNCH
pool-10-thread-2 2021-08-26 18:45:58.448 TRACE com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1  - started...
pool-10-thread-2 2021-08-26 18:45:58.448 TRACE com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1  - synching
TaskExecutorsTest 2021-08-26 18:45:58.448 DEBUG com.triniforce.server.plugins.kernel.TaskExecutorsTest  - waiting for NOTIFIING
TaskExecutorsTest 2021-08-26 18:45:58.448 DEBUG com.triniforce.server.plugins.kernel.TaskExecutorsTest  - shutdown process
pool-10-thread-2 2021-08-26 18:45:58.448 TRACE com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1  - ended...
pool-10-thread-1 2021-08-26 18:45:58.449 TRACE com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1  - started...
pool-10-thread-1 2021-08-26 18:45:58.449 TRACE com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1  - synching
pool-10-thread-1 2021-08-26 18:45:58.449 ERROR com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1  - 
java.lang.InterruptedException
	at java.lang.Object.wait(Native Method)
	at java.lang.Object.wait(Object.java:502)
	at com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1.run(TaskExecutorsTest.java:252)
	at com.triniforce.server.srvapi.InitFinitTaskWrapper.run(InitFinitTaskWrapper.java:32)
	at com.triniforce.server.plugins.kernel.KeyTaskExecutor$KeyTask.run(KeyTaskExecutor.java:60)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at com.triniforce.server.plugins.kernel.KeyTaskExecutor$KeyTask.run(KeyTaskExecutor.java:60)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
pool-10-thread-1 2021-08-26 18:45:58.495 TRACE com.triniforce.server.plugins.kernel.TaskExecutorsTest$T1  - ended...
     * 
     */

}
