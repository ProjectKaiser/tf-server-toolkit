/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.IEnvelopeHandler;

public abstract class OutboxItem {
    final private Object m_data;
    final private IEnvelopeHandler m_replyHandler;
    
    public abstract Object getRecipient(); 
    
    public OutboxItem(Object data, IEnvelopeHandler replyHandler) {
        super();
        m_data = data;
        m_replyHandler = replyHandler;
    }
    public Object getData() {
        return m_data;
    }
    public IEnvelopeHandler getReplyHandler() {
        return m_replyHandler;
    }
    
    public abstract boolean isEmptyRecipient();
    public abstract POBoxWrapper queryTargetBox(PostMaster pm, POBoxWrapper sender);
    
}
