/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

public class LTRGetStreets {
    private StreetPath m_streetPath;
    public LTRGetStreets(){
    }
    public LTRGetStreets(StreetPath streetPath) {
        setStreetPath(streetPath);

    }
    public StreetPath getStreetPath() {
        return m_streetPath;
    }
    public void setStreetPath(StreetPath streetPath) {
        m_streetPath = streetPath;
    }
}
