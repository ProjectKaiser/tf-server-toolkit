/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

public class BusMessage extends BusCmd{
    private final Object m_data;
    private final BusAddress m_to;
    public BusMessage(BusAddress to, Object data){
        m_to = to;
        m_data = data;
    }
    public BusMessage(BusAddress to){
        m_to = to;
        m_data = null;
    }
    public BusAddress getTo(){
        return m_to;
    }
    public Object getData(){
        return m_data;
    }
}
