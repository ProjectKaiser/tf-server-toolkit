/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import com.triniforce.syncbus.intf.ICompleteOffer;
import com.triniforce.syncbus.intf.ICompletePublisher;

public class PubHolder{
    
    private final ICompletePublisher m_ep;
    final Object m_addr;
    private final ICompleteOffer m_eo;
    
    PubHolder(Object addr, ICompletePublisher ep, ICompleteOffer eo){
        m_ep = ep;
        m_addr = addr;
        m_eo = eo;
    }

    public ICompletePublisher getEp() {
        return m_ep;
    }

    public ICompleteOffer getEo() {
        return m_eo;
    }

}
