/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.eventbus;

public class BusMessage extends BusCmd{
    private BusAddressId m_sender;
    private final Object m_data;
    private final BusAddress m_to;
    public BusMessage(BusAddress to, Object data){
        m_to = to;
        m_data = data;
    }
    public BusAddressId getSender(){
        return m_sender;
    }
    public BusAddress getTo(){
        return m_to;
    }
    public Object getData(){
        return m_data;
    }
}
