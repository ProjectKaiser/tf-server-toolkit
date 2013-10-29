/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.ArrayList;
import java.util.Collection;

public class Envelope {
    private String m_boxTo;
    String m_boxFrom;
    Collection<String> m_streetsFrom = new ArrayList<String>();
    private Collection<String> m_streetsTo = new ArrayList<String>();
    
    private IEnvelopeHandler m_callback;
    private IEnvelopeHandler m_recipientHandler;
    
    /**
     * Is called instead of default methods when {@link IOutbox#postBack(Envelope, Object)} is used. 
     */
    public IPOBox callback;
    public Envelope(String addrTo) {
        m_boxTo = addrTo;

    }
    public Envelope(Class addrTo) {
        m_boxTo = addrTo.getName();
    }
    
    public String getAddrTo(){
        return m_boxTo;
    }
    public IEnvelopeHandler getCallback() {
        return m_callback;
    }
    public void setCallback(IEnvelopeHandler callback) {
        m_callback = callback;
    }
    public IEnvelopeHandler getRecipientHandler() {
        return m_recipientHandler;
    }
    public void setRecipientHandler(IEnvelopeHandler recipientHandler) {
        m_recipientHandler = recipientHandler;
    }
    
    /**
     * Called when letter passes IOutbox
     * @param box
     */
    public void leaveBox(String box){
        m_boxFrom = box;
    }
    
    /**
     * Called when a letter leaves street
     * @param street
     * 
     */
    public void leaveStreet(String street){
        m_streetsFrom.add(street);
    }
    public Collection<String> getStreetsTo() {
        return m_streetsTo;
    }
    public void setStreetsTo(Collection<String> streetsTo) {
        m_streetsTo = streetsTo;
    }
    
}
