/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.srvapi;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public abstract class BasicServerTask extends InitFinitTask{

    IBasicServer m_basicServer;
    private String m_threadName;

    boolean b_modeEntered = false;

    public BasicServerTask() {
        m_basicServer = ApiStack.getInterface(IBasicServer.class);
        m_threadName = this.getClass().getName();
    }

    String m_oldThreadName;
    public void init() {
        m_oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(m_threadName);
        m_basicServer.enterMode(IBasicServer.Mode.Running);
        b_modeEntered = true;
        ApiAlgs.getLog(this).trace("Task started: " + getThreadName());
    };

    public void finit() {
        if (b_modeEntered) {
            m_basicServer.leaveMode();
            b_modeEntered = false;
        }
        ApiAlgs.getLog(this).trace("Task finished: " + getThreadName());
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
