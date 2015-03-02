/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.server.srvapi.InitFinitTask;
import com.triniforce.server.srvapi.InitFinitTaskWrapper;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ITime.ITimeHelper;

/**
 * There are two pools, one for big tasks and one for small tasks
 *
 */
public class TaskExecutors implements ITaskExecutors{
    
    public static int MAX_NORMAL_THREADS = 100;

	Map<ExecutorKey, ThreadPoolExecutor> m_executors = new HashMap<ExecutorKey, ThreadPoolExecutor>();
	Integer m_executed = 0;
	
    public TaskExecutors() {
        addExecutor(ITaskExecutors.longTaskExecutorKey, new KeyTaskExecutor(80));
        addExecutor(ITaskExecutors.shortTaskExecutorKey,
                new KeyTaskExecutor(10));
        addExecutor(ITaskExecutors.normalTaskExecutorKey,
                new ThreadPoolExecutor(2, MAX_NORMAL_THREADS, 10, TimeUnit.SECONDS
                        , new SynchronousQueue<Runnable>())//SynchronousQueue prevents flooding
        );
    }
	
	public ThreadPoolExecutor addExecutor(ExecutorKey executorKey,
	        ThreadPoolExecutor executor) {
	    m_executors.put(executorKey, executor);
	    return executor;
	}

    public void awatTermination(long timeOutMS) {
        Long maxTime = ITimeHelper.currentTimeMillis() + timeOutMS;
        for(ExecutorService es: m_executors.values()){
            Long curTimeOut = maxTime -  ITimeHelper.currentTimeMillis();
            try{
                es.awaitTermination(curTimeOut, TimeUnit.MILLISECONDS);
            }catch(Throwable t){
                ApiAlgs.getLog(this).error("Exception during awatTermination", t);
            }
        }
    }

    public Set<ExecutorKey> executorKeys() {
        return m_executors.keySet();
    }

    public void shutdownNow() {
        for(ExecutorService es: m_executors.values()){
            es.shutdownNow();
        }
    }

    public Future execute(ExecutorKey executorKey, InitFinitTask task) throws EExecutorNotFound{
        ThreadPoolExecutor executor = m_executors.get(executorKey);
        if(null == executor){
            throw new EExecutorNotFound(executorKey);
        }
        InitFinitTaskWrapper w = new InitFinitTaskWrapper(task); 
        Future res = executor.submit(w);
        synchronized (m_executed) {
            m_executed++;
        }
        return res;
    }

    public ThreadPoolExecutor getExecutor(ExecutorKey key) {
        return m_executors.get(key);
    }

    public int getCompletedTasksCount() {
        int res = 0;
        for(ThreadPoolExecutor es: m_executors.values()){
            res += es.getCompletedTaskCount();
        }
        return res;
    }

    public int getTasksCount(){
        synchronized (m_executed) {
            return m_executed;
        }
    }
    
}
