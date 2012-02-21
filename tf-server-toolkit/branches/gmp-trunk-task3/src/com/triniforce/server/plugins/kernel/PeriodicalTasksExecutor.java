/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IFinitable;
import com.triniforce.utils.IInitable;

/**
 * Tested by Server_PeriodicalTasksTest
 */
public class PeriodicalTasksExecutor implements IFinitable {

    ScheduledThreadPoolExecutor m_executor = new ScheduledThreadPoolExecutor(4);

    public static class BasicPeriodicalTask implements IInitable, IFinitable,
            Runnable {
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

        IBasicServer m_basicServer;

        boolean b_modeEntered = false;

        public BasicPeriodicalTask() {
            m_basicServer = ApiStack.getInterface(IBasicServer.class);
        }

        public void init() {
            m_basicServer.enterMode(IBasicServer.Mode.Running);
            b_modeEntered = true;
        };

        public void finit() {
            if (b_modeEntered) {
                m_basicServer.leaveMode();
                b_modeEntered = false;
            }
        }

        public void run() {
        };
    }

    public static class PeriodicalTaskWrapper implements Runnable {

        private final BasicPeriodicalTask m_command;

        public PeriodicalTaskWrapper(BasicPeriodicalTask command) {
            m_command = command;
        }

        public void run() {
            try {
                try {
                    m_command.init();
                } catch (Throwable e) {
                    ApiAlgs.getLog(this).error(
                            "Initialization error:" + m_command.toString(), e);//$NON-NLS-1$
                    return;
                }
                try {
                    m_command.run();
                } catch (Throwable e) {
                    ApiAlgs.getLog(this).error(
                            "Run error:" + m_command.toString(), e);//$NON-NLS-1$
                }
            } finally {
                try {
                    m_command.finit();
                } catch (Throwable e) {
                    ApiAlgs.getLog(this).error(
                            "Finit error:" + m_command.toString(), e);//$NON-NLS-1$
                }
            }
        }
    }

    public void scheduleWithFixedDelay(BasicPeriodicalTask command,
            long initialDelay, long delay, TimeUnit unit) {
        m_executor.scheduleWithFixedDelay(new PeriodicalTaskWrapper(command),
                initialDelay, delay, unit);
    }

    public void finit() {
        m_executor.shutdownNow();
        try {
            m_executor.awaitTermination(600, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            ApiAlgs.rethrowException(e);
        }
        if (!m_executor.isTerminated()) {
            ApiAlgs.getLog(this).error(
                    "Note: not all tasks have been terminated");//$NON-NLS-1$
        }
    }

}
