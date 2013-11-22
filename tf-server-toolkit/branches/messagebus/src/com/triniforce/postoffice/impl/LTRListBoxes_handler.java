/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.triniforce.postoffice.intf.LTRListBoxes;


public class LTRListBoxes_handler {
    static Object process(PostMaster pm, EnvelopeCtx ctx, Object data){
        
        LTRListBoxes ltr = (LTRListBoxes) data;
        Street ws = pm.m_rootStreet.queryPath(ltr.getStreetPath());
        if( null == ws){
            return null;
        }
        NamedPOBoxWrappers boxws = ws.getBoxes();
        
        Map<String, UUID> res = new HashMap<String, UUID>();
        
        for(String name: boxws.keySet()){
            POBoxWrapper boxw = boxws.get(name);
            if(null == boxw){
                continue;
            }
            res.put(name, boxw.getUuid());
        }
        
        return res;
        
    }

}
