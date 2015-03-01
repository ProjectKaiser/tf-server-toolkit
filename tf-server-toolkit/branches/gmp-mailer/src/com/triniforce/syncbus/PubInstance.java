/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import com.triniforce.syncbus.intf.IPublisher;

public class PubInstance{
    
    final IPublisher m_ep;
    final Object m_addr;
    
    PubInstance(Object addr, IPublisher ep){
        m_ep = ep;
        m_addr = addr;
    }

    public IPublisher getEp() {
        return m_ep;
    }

	public Object getAddr() {
		return m_addr;
	}


}
