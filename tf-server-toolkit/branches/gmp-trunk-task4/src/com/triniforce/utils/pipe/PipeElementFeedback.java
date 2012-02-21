/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.pipe;

public class PipeElementFeedback implements IPipeElementFeedback{

    protected boolean m_stopped;
    
    public boolean isStopped() {
        return m_stopped;
    }

    public void setStopped(boolean stopped) {
        m_stopped = stopped;
    }

}
