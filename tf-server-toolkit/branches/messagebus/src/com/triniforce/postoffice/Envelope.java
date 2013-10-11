/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

public class Envelope {
    private String m_addrTo;
    String m_addrFrom;
    /**
     * Is called instead of default methods when {@link IOutbox#postBack(Envelope, Object)} is used. 
     */
    public IRecipient callback;
    public Envelope(String addrTo) {
        m_addrTo = addrTo;

    }
    public Envelope(Class addrTo) {
        m_addrTo = addrTo.getName();
    }
    
    public String getAddrTo(){
        return m_addrTo;
    }
    
}
