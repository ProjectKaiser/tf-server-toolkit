/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.UUID;

import com.triniforce.postoffice.intf.LTRAddStreet;

public class LTRAddStreet_handler{
    static Object process(PostMaster pm, EnvelopeCtx ctx, Object data){
        
        LTRAddStreet ltr = (LTRAddStreet) data;
        
        Street ws = pm.m_rootStreet;
        
        Street parentStreet = ws.queryPath(ltr.getStreetPath());
        if(null == parentStreet){
            return null;
        }
        
        Street newStreet = new Street(ltr.getBoxes());
        
        parentStreet.getStreets().put(ltr.getStreetName(), newStreet);
        
        for(POBoxWrapper boxw: newStreet.getBoxes().values()){
            UUID uuid = UUID.randomUUID();
            boxw.setUuid(uuid);
            pm.m_boxWrappers.put(uuid, boxw);
        }
        
        return null;
    }

}
