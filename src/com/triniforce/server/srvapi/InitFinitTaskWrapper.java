/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.srvapi;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ICommitable;

public class InitFinitTaskWrapper implements Runnable  {

    private final InitFinitTask m_command;
    private Boolean isErrorState = false;

    public InitFinitTaskWrapper(InitFinitTask command) {
        m_command = command;
    }
    
	public void run() {
        try {
        	Boolean executed = false;
            try {
                try {
                    m_command.init();
                } catch (Throwable e) {
                    enterErrorState("Initialization error:" + m_command.toString(), e);
                    return;
                }
                try {
                    m_command.run();
                    if (m_command instanceof ICommitable) {
                        ((ICommitable) m_command).commit();
                    }
                    executed = true;
                } catch (Throwable e) {
                	 enterErrorState("Run error:" + m_command.toString(), e); //$NON-NLS-1$
                }
            } finally {
                try {
                    m_command.finit();
                    if (executed) {
                    	leaveErrorState();
                    }
                } catch (Throwable e) {
                	enterErrorState("Finit error:" + m_command.toString(), e); //$NON-NLS-1$
                }
            }
        } catch (Throwable t) {
            try {
                System.err.println("Fatal error executing " + (m_command != null?m_command.getClass().toString():"null")+ ": " + t);
                t.printStackTrace();
            } catch (Throwable tt) {

            }
        }
    }

    private void leaveErrorState() {
    	if (isErrorState) {
    		ApiAlgs.getLog(this).trace("periodical task failure mode leave: " + getTaskName());
    		isErrorState = false;
    	}
	}
    
    private void enterErrorState(String msg, Throwable e) {
		if (!isErrorState) {
			ApiAlgs.logError(this, msg, e);
			ApiAlgs.getLog(this).trace("periodical task failure mode enter: " + getTaskName());
			isErrorState = true;
		}
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_command == null) ? 0 : m_command.hashCode());
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
        InitFinitTaskWrapper other = (InitFinitTaskWrapper) obj;
        if (m_command == null) {
            if (other.m_command != null)
                return false;
        } else if (!m_command.equals(other.m_command))
            return false;
        return true;
    }

    
    public String getTaskName(){
    	return "TaskWrapper." + m_command.getClass().getName();
    }
}
