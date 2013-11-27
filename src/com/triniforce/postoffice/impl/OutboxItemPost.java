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

    @Override
    public boolean isEmptyRecipient() {
        return null == m_addr || m_addr.length() == 0;
    }

    @Override
    public POBoxWrapper queryTargetBox(PostMaster pm, POBoxWrapper sender) {
        return pm.queryTargetBox(sender.getParent(), null, m_addr);
    }

}
