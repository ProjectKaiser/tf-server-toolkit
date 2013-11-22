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

    public PostTask(PostMaster pm, UUID sender, StreetPath streetPath, String box, Object data, IEnvelopeHandler replyHandler) {
        m_pm = pm;
        m_sender = sender;
        m_streetPath = streetPath;
        m_box = box;
        m_data = data;
        m_replyHandler = replyHandler;
    }
    public Object call(){
        
        EnvelopeCtx ctx = new EnvelopeCtx(new Envelope(m_sender, m_replyHandler));
        
        if(null == m_streetPath){
            return m_pm.dispatch(ctx, m_data);
        }
        
        return null;
    }
}