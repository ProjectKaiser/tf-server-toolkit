/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.services;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.concurrent.locks.ReentrantLock;

import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ICheckInterrupted;
import com.triniforce.utils.ICheckInterrupted.EInterrupted;

/**
 * cycle pause = 10 sec
 * <p>
 * exception pause = 60 secs
 */
public class Service implements IService, ICycledThreadLogic, Runnable {

    protected State m_state = State.STOPPED;

    protected IBasicServer m_server;

    protected Thread m_thread;

    protected static ReentrantLock m_lockTermination = new ReentrantLock();
    
    public static void lockTermination(){
        m_lockTermination.lock();        
    }
    public static void unlockTermination(){
        m_lockTermination.unlock();
    }

    public void doCycle() throws Throwable {
    }

    public void finit() {
    }

    public void finitCycle() {
    }

    public int getCycleExceptionPauseMs() {
        return 1000 * 60;
    }

    public void init() {
       
    }

    public void initCycle() {

    }

    public void stop() {
        if (getState() != State.RUNNING)
            return;
        setState(State.STOPPING);

        if (m_thread.equals(Thread.currentThread())) {
            /*
             * Cope with bug in log4j - it cleans "terminated" flag on linux, so
             * do no call waitForAnyState, since it uses logging functions
             */
            ICheckInterrupted.Helper.sleep(100);// this just raises an exception
        }
        waitForAnyState(EnumSet.of(IService.State.STOPPED), true);
    }

    public int getCyclePauseMs() {
        return 1000 * 10;
    }

    protected void popPush() {
        ISrvSmartTranFactory tf = ApiStack.getApi().getIntfImplementor(
                ISrvSmartTranFactory.class);
        tf.pop();
        tf.push();
    }

    protected void finitWithCatch() {
        try {
            finit();
        } catch (Throwable t) {
            ApiAlgs.getLog(this).error("Service.finit() problem", t);//$NON-NLS-1$
            return;
        }
    }

    protected void runAndCatchProblems() {
        try {
            init();
        } catch (Throwable t) {
            ApiAlgs.getLog(this).error("Service.init() problem", t);//$NON-NLS-1$
            finitWithCatch();
            return;
        }
        setState(State.RUNNING);
        try {
            ICycledThreadLogic.Runner.runCTL(this);
        } catch (Throwable t) {
            ApiAlgs.getLog(this).error("CTL.Runner problem", t);//$NON-NLS-1$                        
        }
        finitWithCatch();
    }

    public void run() {
        try {
            Thread.currentThread().setName(this.getName());

            /*
             * We do not have ILogger here, so we do not log
             */

            {// push ICheckInterrupted
                Api api = new Api();
                api.setIntfImplementor(ICheckInterrupted.class,
                        new ICheckInterrupted.CheckInterrupted());
                ApiStack.pushApi(api);
            }
            try {
                m_server.enterMode(Mode.Running);
                ISrvSmartTranFactory tf = ApiStack.getApi().getIntfImplementor(
                        ISrvSmartTranFactory.class);
                tf.pop();
                try {
                    runAndCatchProblems();
                } finally {
                    try {
                        m_server.leaveMode();
                    } finally {
                        setState(State.STOPPED);
                    }
                }
            } finally {
                // pop ICheckInterrupted
                ApiStack.popApi();
            }
        } catch (Throwable t) {
            ApiAlgs.getLog(this).error("Service.run() problem", t);//$NON-NLS-1$
            return;
        }

    }

    protected void waitForAnyState(EnumSet states, boolean bInterrupt)
            throws EInterrupted {
        IThrdWatcherRegistrator  itwr  = ApiStack.getInterface(IThrdWatcherRegistrator .class);
        itwr.registerLongTermOp(Thread.currentThread());
        try {
            while (true) {
                ApiAlgs
                        .getLog(this)
                        .trace(
                                MessageFormat
                                        .format(
                                                "Waiting for {0} (thread={1}", states, m_thread));//$NON-NLS-1$                
                if (states.contains(getState()))
                    break;
                if (bInterrupt) {
                    /*
                     * Cope with bug in log4j - it cleans "terminated" flag on
                     * linux. If thread does logging it can lost "interrupted"
                     * flag
                     * 
                     */
                    lockTermination();
                    try {
                        m_thread.interrupt();
                    } finally {
                        unlockTermination();
                    }
                }
                ICheckInterrupted.Helper.sleep(500);
            }
        } finally {
            ApiAlgs.getLog(this).trace("Came to " + getState());//$NON-NLS-1$
            itwr.unregisterLongTermOp(Thread.currentThread());
        }
    }

    public void start() {
        if (getState() != State.STOPPED)
            return;
        setState(IService.State.STARTING);
        m_server = ApiStack.getInterface(IBasicServer.class);
        m_thread = new Thread(this);
        m_thread.start();
        waitForAnyState(EnumSet.of(IService.State.RUNNING,
                IService.State.STOPPED), false);
    }

    public State getState() {
        return m_state;
    }

    public void setState(State state) {
        m_state = state;
    }

    void join() {
        if (m_thread != null) {
            try {
                m_thread.join();
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
        }
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

//    protected Long m_id;
//
//    public void setId(Long id) {
//        m_id = id;
//    }
//
//    public long getId() {
//        return m_id;
//    }

    public void commitCycle() {
    }
}
