/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;


public class LTRDisconnectStreet {
    private StreetPath m_streetPath;
    
    public LTRDisconnectStreet(){
        
    }
    
    public LTRDisconnectStreet(StreetPath streetPath){
        m_streetPath = streetPath;
    }

    public StreetPath getStreetPath() {
        return m_streetPath;
    }

}
