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
    private String m_hint;
    
    public UpgradeProcedure() {
        setHint(this.getClass().getName());
    }

    /**
     * @param hint Human-readable description to be shown to user. 
     * Must be in form "Upgrade data in table A".
     * 
     */
    public UpgradeProcedure(String hint){
        super();
        setHint(hint);        
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

    public void setHint(String hint) {
        m_hint = hint;
    }

}
