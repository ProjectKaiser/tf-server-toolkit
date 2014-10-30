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

    public InitFinitTaskWrapper(InitFinitTask command) {
        m_command = command;
    }
    
    public static boolean isSeriousException(Throwable e){
        if(null == e){
            return true;
        }
        if(null == e.getMessage()){
            return true;
        }
        if(e.getMessage().contains("network request to host")){
            return false;
        }
        return true;
    }
    
    void logInitializationErrror(Throwable e){
        String msg = "Initialization error:" + m_command.toString();
        if(isSeriousException(e)){
            ApiAlgs.getLog(this).error(msg, e);
        }else{
            ApiAlgs.getLog(this).trace(msg, e);
        }
    }

    public void run() {
        try {
            try {
                m_command.init();
            } catch (Throwable e) {
                logInitializationErrror(e);
                return;
            }
            try {
                m_command.run();
                if(m_command instanceof ICommitable){
                    ((ICommitable) m_command).commit();
                }
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

}
