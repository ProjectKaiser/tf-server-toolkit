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
public class LTRListStreets extends LTRGet<List<String>>{
    private StreetPath m_streetPath;
    public LTRListStreets(){
    }
    public LTRListStreets(StreetPath streetPath) {
        setStreetPath(streetPath);
    }
    public LTRListStreets(String... streets) {
        setStreetPath(new StreetPath(streets));
    }
    public StreetPath getStreetPath() {
        return m_streetPath;
    }
    public void setStreetPath(StreetPath streetPath) {
        m_streetPath = streetPath;
    }
}
