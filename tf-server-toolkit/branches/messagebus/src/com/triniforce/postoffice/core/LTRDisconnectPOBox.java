/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.core;


public class LTRDisconnectPOBox extends LTRDisconnectStreet{
    private String m_box;
    private final StreetPath m_streetPath2;
    
    public LTRDisconnectPOBox(StreetPath streetPath, String box) {
        m_streetPath2 = streetPath;
        m_box = box;
     
    }

    public String getBox() {
        return m_box;
    }

    public void setBox(String box) {
        m_box = box;
    }

    public StreetPath getStreetPath2() {
        return m_streetPath2;
    }

}
