/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

import java.util.List;

/**
 * Expects copy of street names in List<String>  
 */
public class LTRGetStreets extends LTRGet<List<String>>{
    private StreetPath m_streetPath;
    public LTRGetStreets(){
    }
    public LTRGetStreets(StreetPath streetPath) {
        setStreetPath(streetPath);
    }
    public LTRGetStreets(String... streets) {
        setStreetPath(new StreetPath(streets));
    }
    public StreetPath getStreetPath() {
        return m_streetPath;
    }
    public void setStreetPath(StreetPath streetPath) {
        m_streetPath = streetPath;
    }
}
