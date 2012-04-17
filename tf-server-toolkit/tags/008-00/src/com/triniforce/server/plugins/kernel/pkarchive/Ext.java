/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.pkarchive;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Ext implements Serializable{
    Map<String, ExtEntry> m_extEntries = new HashMap<String, ExtEntry>();

    public Map<String, ExtEntry> getExtEntries() {
        return m_extEntries;
    }

    public void setExtEntries(Map<String, ExtEntry> extEntries) {
        m_extEntries = extEntries;
    }
    
}