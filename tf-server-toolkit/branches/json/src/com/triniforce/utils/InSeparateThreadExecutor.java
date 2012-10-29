/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import com.triniforce.utils.ITime.ITimeHelper;

public class InSeparateThreadExecutor {
    
    public interface IRunnable{
        public void run() throws Exception;
    }
    
    @SuppressWarnings("serial")
    public static class EExecutorTimeoutExpired extends RuntimeException{
        public EExecutorTimeoutExpired(String name) {
            super("InSeparateThreadExecutor: execution timeout expired for '" + name + "'");
        }
    }
    
    public static class ExecutionResult{
        public boolean timeoutExpired;
        public Throwable exception;
        protected Thread t;
    }
    
    private ExecutionResult m_res;
    
    public ExecutionResult execute(final String name, final IRunnable r, long timeoutMs){
        
        TFUtils.assertTrue(timeoutMs >0, "Wtong timeout");
        
        m_res = new ExecutionResult();
        Runnable myRunnable = new Runnable(){

            public void run() {
                Thread.currentThread().setName(InSeparateThreadExecutor.class.getSimpleName() + ":" + name);                
                try{
                    r.run();
                }catch(Throwable t){
                    if(!ICheckInterrupted.Helper.isInterruptedException(t)){
                        ApiAlgs.getLog(InSeparateThreadExecutor.class).trace("Exception in InSeparateThreadExecutor", t);
                        m_res.exception = t;
                    }
                }
            }
        };
        
        m_res.t = new Thread(myRunnable);
        m_res.t.start();
        long start = ITimeHelper.currentTimeMillis();
        while(true){
            if(!m_res.t.isAlive()){
                break;
            }
            if(ITimeHelper.currentTimeMillis() - start > timeoutMs){
                m_res.timeoutExpired = true;
                m_res.t.interrupt();
                break;
            }
            try{
                ICheckInterrupted.Helper.sleep(100);
            }catch(RuntimeException re){
                if(ICheckInterrupted.Helper.isInterruptedException(re)){
                    m_res.t.interrupt();
                    throw re;
                }
            }
        }
        if(null != m_res.exception){
            if( m_res.exception instanceof RuntimeException){
                throw (RuntimeException) m_res.exception; 
            }
            throw new ApiAlgs.RethrownException(m_res.exception);
        }
        if(m_res.timeoutExpired){
            throw new EExecutorTimeoutExpired(name);
        }
        return m_res;
    }

    public ExecutionResult getResult() {
        return m_res;
    }

}
