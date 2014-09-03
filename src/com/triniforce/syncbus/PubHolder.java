/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import com.triniforce.syncbus.intf.ICOffer;
import com.triniforce.syncbus.intf.ICPublisher;

public class PubHolder{
    
    private final ICPublisher m_ep;
    final Object m_addr;
    private final ICOffer m_eo;
    
    PubHolder(Object addr, ICPublisher ep, ICOffer eo){
        m_ep = ep;
        m_addr = addr;
        m_eo = eo;
    }

    public ICPublisher getEp() {
        return m_ep;
    }

    public ICOffer getEo() {
        return m_eo;
    }

}
