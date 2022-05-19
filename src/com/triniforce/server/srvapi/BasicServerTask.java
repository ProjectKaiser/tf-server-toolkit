/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.srvapi;

import java.util.concurrent.TimeUnit;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ICommitable;
import com.triniforce.utils.ITime;

public abstract class BasicServerTask extends InitFinitTask implements ICommitable{

    IBasicServer m_basicServer;
    private String m_threadName;

    boolean b_modeEntered = false;
    
    Class logClass;
	private ITime m_time;
    
    
    public void commit(){
        ISrvSmartTranFactory.Helper.commit();
    }

    public BasicServerTask() {
        m_basicServer = ApiStack.getInterface(IBasicServer.class);
        m_threadName = this.getClass().getName();
        m_time = ApiStack.getInterface(ITime.class);
    }

    String m_oldThreadName;
	private boolean  m_bLogInitFinit;
	private long m_lastLoggedTime=0L;
	
    public void init() {
        m_oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(m_threadName);
        
        long currentTime = m_time.currentTimeMillis(); 
        m_bLogInitFinit = (currentTime - getLastLoggedTime()) > TimeUnit.HOURS.toMillis(1); 
        
        if(m_bLogInitFinit){
	        logClass = ApiAlgs.calcEnabledTraceClass(m_basicServer.getCoreApi(), this.getClass());
	        if(null != logClass){
	            ApiAlgs.getLog(m_basicServer.getCoreApi(), logClass).trace("Task started: " + getThreadName());
	            setLastLoggedTime(currentTime);
	        }
        }

        m_basicServer.enterMode(IBasicServer.Mode.Running);
        b_modeEntered = true;
                
        IThrdWatcherRegistrator twr = ApiStack.getInterface(IThrdWatcherRegistrator.class);
        twr.registerThread(Thread.currentThread(), null);
        
    };

    private void setLastLoggedTime(long currentTime) {
    	m_lastLoggedTime = currentTime;
		
	}

	private long getLastLoggedTime() {
		return m_lastLoggedTime;
	}

	public void finit() {
        if (b_modeEntered) {
            IThrdWatcherRegistrator twr = ApiStack.getInterface(IThrdWatcherRegistrator.class);
            twr.unregisterThread(Thread.currentThread());
            m_basicServer.leaveMode();
            b_modeEntered = false;
        }
        if(m_bLogInitFinit && null != logClass){
            ApiAlgs.getLog(m_basicServer.getCoreApi(), logClass).trace("Task finished: " + getThreadName());
        }
        Thread.currentThread().setName(m_oldThreadName);
    }

    public abstract void run();

    public void setThreadName(String threadName) {
        m_threadName = threadName;
    }

    public String getThreadName() {
        return m_threadName;
    }

}
