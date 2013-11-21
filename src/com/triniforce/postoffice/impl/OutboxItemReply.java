/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.Envelope;
import com.triniforce.postoffice.intf.IEnvelopeHandler;

public class OutboxItemReply extends OutboxItem {

    private final Envelope m_envelope;

    public OutboxItemReply(Envelope envelope, Object data, IEnvelopeHandler replyHandler) {
        super(data, replyHandler);
        // TODO Auto-generated constructor stub
        m_envelope = envelope;
    }

    @Override
    public Object getRecipient() {
        return m_envelope;
    }
    

}
