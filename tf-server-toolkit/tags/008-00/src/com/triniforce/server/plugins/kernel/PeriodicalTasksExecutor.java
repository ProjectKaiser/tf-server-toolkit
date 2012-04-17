/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.triniforce.server.srvapi.BasicServerTask;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.server.srvapi.InitFinitTaskWrapper;
import com.triniforce.utils.IFinitable;

/**
 * Tested by Server_PeriodicalTasksTest
 */
public class PeriodicalTasksExecutor implements IFinitable {

    private final ITaskExecutors m_te = new TaskExecutors();

    public PeriodicalTasksExecutor(){
    }
    

    public abstract static class BasicPeriodicalTask extends BasicServerTask {
        public long initialDelay = 60000;

        public long delay = 60000;

        public TimeUnit unit = TimeUnit.MILLISECONDS;

        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + (int) (delay ^ (delay >>> 32));
            result = PRIME * result
                    + (int) (initialDelay ^ (initialDelay >>> 32));
            result = PRIME * result + ((unit == null) ? 0 : unit.hashCode());
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
            final BasicPeriodicalTask other = (BasicPeriodicalTask) obj;
            if (delay != other.delay)
                return false;
            if (initialDelay != other.initialDelay)
                return false;
            if (unit == null) {
                if (other.unit != null)
                    return false;
            } else if (!unit.equals(other.unit))
                return false;
            return true;
        }

    }

    public static class PeriodicalTaskWrapper extends InitFinitTaskWrapper {
		public PeriodicalTaskWrapper(BasicPeriodicalTask command) {
			super(command);
		}
	}

    public void scheduleWithFixedDelay(BasicPeriodicalTask command,
            long initialDelay, long delay, TimeUnit unit) {
        ScheduledThreadPoolExecutor spe = (ScheduledThreadPoolExecutor) getTe().getExecutor(ITaskExecutors.periodicalTaskExecutorKey);
        spe.scheduleWithFixedDelay(new PeriodicalTaskWrapper(command),
                initialDelay, delay, unit);
    }

    public static final int TASKS_TIMEOUT_MS = 1000 * 30;
    public void finit() {
        getTe().shutdownNow();
        getTe().awatTermination(TASKS_TIMEOUT_MS);
    }
    public ITaskExecutors getTe() {
        return m_te;
    }

}
