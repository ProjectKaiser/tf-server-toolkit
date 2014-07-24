/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import com.triniforce.syncbus.intf.IEntireOffer;
import com.triniforce.syncbus.intf.IEntirePub;

public class PubHolder{
    
    private final IEntirePub m_ep;
    final Object m_addr;
    private final IEntireOffer m_eo;
    
    PubHolder(Object addr, IEntirePub ep, IEntireOffer eo){
        m_ep = ep;
        m_addr = addr;
        m_eo = eo;
    }

    public IEntirePub getEp() {
        return m_ep;
    }

    public IEntireOffer getEo() {
        return m_eo;
    }

}
