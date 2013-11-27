/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.UUID;
import java.util.concurrent.Callable;

import com.triniforce.postoffice.intf.Envelope;
import com.triniforce.postoffice.intf.IEnvelopeHandler;
import com.triniforce.postoffice.intf.StreetPath;

@SuppressWarnings("unused")
public class PostTask implements Callable{
    private final Object m_data;
    private final IEnvelopeHandler m_replyHandler;
    private final PostMaster m_pm;
    private final UUID m_sender;
    private final IEnvelopeHandler m_recipientHandler;
    private final UUID m_recipient;

    public PostTask(PostMaster pm, UUID sender, IEnvelopeHandler replyHandler, UUID recipient, Object data, IEnvelopeHandler recipientHandler){
        m_pm = pm;
        m_sender = sender;
        m_recipient = recipient;
        m_data = data;
        m_replyHandler = replyHandler;
        m_recipientHandler = recipientHandler;
    }
    
    public Object call(){
        
        EnvelopeCtx ctx = new EnvelopeCtx(new Envelope(m_sender, m_replyHandler));

        Outbox out = new Outbox(m_sender);
        Object res = null;
        
        if(null == m_recipient){
            return null;
        }
        
        POBoxWrapper boxwRecipient = m_pm.m_boxWrappers.get(m_recipient);
        if( null == boxwRecipient){
            return null;
        }
        
        boxwRecipient.getBox().process(ctx, m_data, out);
        
        //process Outboxes
        
        for(OutboxItem oi: out.getItems()){
            if( oi.isEmptyRecipient()){
                res = oi.getData();
            }else{
                
                //m_pm.queryTargetBox(boxw, nulltargetStreetPath, targetBox);
            }
        }
        
        return res;
    }
}