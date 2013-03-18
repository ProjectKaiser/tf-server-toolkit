/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.service;

import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ICheckInterrupted;

public interface EP_ICycledThreadLogic {

    int getCycleExceptionPauseMs();

    int getCyclePauseMs();

    void init();

    /**
     * Called only if init() has not thrown exception
     */
    void finit();

    void initCycle();

    /**
     * Called only if initCycle() has not thrown exception
     * 
     */
    void doCycle() throws Throwable;
    
    /**
     * Called after doCycle() if no exception was raised 
     */
    void commitCycle();

    /**
     * Called only if initCycle() has not thrown exception
     * 
     */
    void finitCycle();

    /**
     * Class to play CycledThreadLogicwhich interface
     */
    public static class Runner {
        public static void runCTL(EP_ICycledThreadLogic ctl) {
            while (true) {
                try {
                    IThrdWatcherRegistrator twr = ApiStack.queryInterface(IThrdWatcherRegistrator.class);
                    try {
                        if(null != twr){
                            twr.registerThread(Thread.currentThread(), "ICycledThreadLogic");
                        }
                        ctl.initCycle();                        
                        ctl.doCycle();
                        ctl.commitCycle();
                    } finally {
                        try{
                            ctl.finitCycle();
                        }finally{
                            if(null != twr){
                                twr.unregisterThread(Thread.currentThread());
                            }
                        }
                    }
                    ICheckInterrupted.Helper.sleep(ctl.getCyclePauseMs());
                } catch (Throwable t) {
                    if (ICheckInterrupted.Helper.isInterruptedException(t)) {
                        break;
                    }
                    ApiAlgs.getLog(ctl).error("Error in CTL", t);//$NON-NLS-1$
                    try {
                        ICheckInterrupted.Helper.sleep(ctl
                                .getCycleExceptionPauseMs());
                    } catch (Throwable tt) {
                        break;
                    }                    
                }
                
            }
        }
    }

}

