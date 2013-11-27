/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.postoffice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.triniforce.postoffice.intf.Envelope;
import com.triniforce.postoffice.intf.IEnvelopeCtx;
import com.triniforce.postoffice.intf.IEnvelopeHandler;
import com.triniforce.postoffice.intf.IOutbox;

public class Outbox implements IOutbox {

    private UUID m_defSender;

    public Outbox(UUID defSender) {
        setDefSender(defSender);
    }
    
    private final List<OutboxItem> m_items = new ArrayList<OutboxItem>();

    public void post(String addr, Object data, IEnvelopeHandler replyHandler) {
        getItems().add(new OutboxItemPost(addr, data, replyHandler));

    }

    public void post(Class addr, Object data, IEnvelopeHandler replyHandler) {
        getItems().add(new OutboxItemPost(addr.getName(), data, replyHandler));

    }

    public void reply(Envelope envelope, Object data,
            IEnvelopeHandler replyHandler) {
        getItems().add(new OutboxItemReply(envelope, data, replyHandler));
    }

    public List<OutboxItem> getItems() {
        return m_items;
    }

    public void reply(IEnvelopeCtx envCtx, Object data,
            IEnvelopeHandler replyHandler) {
        reply(envCtx.getEnvelope(), data, replyHandler);
    }

    public UUID getDefSender() {
        return m_defSender;
    }

    public void setDefSender(UUID defSender) {
        m_defSender = defSender;
    }

}
