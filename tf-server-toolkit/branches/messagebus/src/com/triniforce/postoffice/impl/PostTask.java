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
    private final StreetPath m_streetPath;
    private final String m_box;
    private final PostMaster m_pm;
    private final UUID m_sender;
    private final IEnvelopeHandler m_targetHandler;

    public PostTask(PostMaster pm, UUID sender, IEnvelopeHandler replyHandler, StreetPath targetStreetPath, String targetBox, Object data, IEnvelopeHandler targetHandler){
        m_pm = pm;
        m_sender = sender;
        m_streetPath = targetStreetPath;
        m_box = targetBox;
        m_data = data;
        m_replyHandler = replyHandler;
        m_targetHandler = targetHandler;
    }
    public Object call(){
        
        EnvelopeCtx ctx = new EnvelopeCtx(new Envelope(m_sender, m_replyHandler));

        Outboxes outs = new Outboxes();
        Object res = null;
        
        if(null == m_streetPath){
            res =  m_pm.process(ctx, m_data, outs);
        }else{
            
        }
        
        return res;
    }
}