/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.UUID;

public class Envelope {
    final private UUID m_sender;
    final private IEnvelopeHandler m_callback;
    
    public Envelope(UUID sender, IEnvelopeHandler callback) {
        m_sender = sender;
        m_callback = callback;

    }

    public IEnvelopeHandler getCallback() {
        return m_callback;
    }

    public UUID getSender(){
        return m_sender;
    }

}
