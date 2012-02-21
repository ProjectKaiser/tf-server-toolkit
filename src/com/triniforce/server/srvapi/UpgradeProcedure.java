/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import com.triniforce.utils.Entity;

/**
 * Represents data upgrade procedure
 *
 */
public class UpgradeProcedure extends Entity{
    private final String m_hint;

    /**
     * @param hint Human-readable description to be shown to user. 
     * Must be in form "Upgrade data in table A".
     * 
     */
    public UpgradeProcedure(String hint){
        super();
        m_hint = hint;        
    }
    
    /**
     * Descendants override this method to update database data 
     * @throws Exception 
     */
    public void run() throws Exception{        
    }
    
    public String getHint() {
        return m_hint;
    }

}
