/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

/**
 * Subscribe for single event
 */
public class SRAskForSingleEvent extends SubscrResponse{
    private final Object m_addr;
    
    public SRAskForSingleEvent(Object addr) {
        m_addr = addr;
    }

    public Object getAddr() {
        return m_addr;
    }

}
