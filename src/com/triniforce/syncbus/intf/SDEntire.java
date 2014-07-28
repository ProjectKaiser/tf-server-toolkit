/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public class SDEntire extends SubscrData{
    
    private final boolean m_isIncremental;
    private final Object m_data;

    public boolean isIncremental() {
        return m_isIncremental;
    }
    
    public SDEntire(boolean m_isIncremental,  Object m_data){
        this.m_isIncremental = m_isIncremental;
        this.m_data = m_data;
    }

    public Object getData() {
        return m_data;
    }

}
