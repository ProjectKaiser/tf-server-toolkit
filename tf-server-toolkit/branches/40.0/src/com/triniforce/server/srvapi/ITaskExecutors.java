/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.srvapi;

import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Intended to submit tasks periodically. If task with such key exists in internal map it is ignored.
 * Class keeps two queues - one for long tasks, one for short tasks
 *
 */
/**
 *
 */
public interface ITaskExecutors {
    
    
    Future execute(ExecutorKey executorKey, InitFinitTask task) throws RejectedExecutionException;
	Set<ExecutorKey> executorKeys();
	ThreadPoolExecutor getExecutor(ExecutorKey key);
	void shutdownNow();
	
	int getCompletedTasksCount();
	
	//number of calls to execute
	int getTasksCount();
	
	void awatTermination(long timeOutMS);
	
    public static class EExecutorNotFound extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EExecutorNotFound(Object key) {
            super(MessageFormat.format("Executor with key {0} not found", key));
        }
    }
    
    public abstract static class ExecutorKey{
        String m_field = this.getClass().getName();

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((m_field == null) ? 0 : m_field.hashCode());
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
            ExecutorKey other = (ExecutorKey) obj;
            if (m_field == null) {
                if (other.m_field != null)
                    return false;
            } else if (!m_field.equals(other.m_field))
                return false;
            return true;
        }
    }

    public static class LongTaskExecutorKey extends ExecutorKey{};
    public static class ShortTaskExecutorKey extends ExecutorKey{};
    public static class PeriodicalTaskExecutorKey extends ExecutorKey{};
    public static class NormalTaskExecutorKey extends ExecutorKey{};
    
    public static final ExecutorKey longTaskExecutorKey = new LongTaskExecutorKey();
    public static final ExecutorKey shortTaskExecutorKey = new ShortTaskExecutorKey();
    public static final ExecutorKey periodicalTaskExecutorKey = new PeriodicalTaskExecutorKey();
    
    /**
     * Fixed thread pool task
     */
    public static final ExecutorKey normalTaskExecutorKey = new NormalTaskExecutorKey();
	
}
