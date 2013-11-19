/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.IEnvelopeHandler;

public class OutboxItemPost extends OutboxItem {

    private final String m_addr;

    public OutboxItemPost(String addr, Object data, IEnvelopeHandler replyHandler){
        super(data, replyHandler);
        m_addr = addr;
    }

    @Override
    public String getRecipient(){
        return m_addr;
    }

}
