/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.Envelope;
import com.triniforce.postoffice.intf.IEnvelopeCtx;

public class EnvelopeCtx implements IEnvelopeCtx{

    
    private final Envelope m_env;

    public EnvelopeCtx(Envelope env) {
        m_env = env;
    }
    
    public Envelope getEnvelope() {
        return m_env;
    }

}
