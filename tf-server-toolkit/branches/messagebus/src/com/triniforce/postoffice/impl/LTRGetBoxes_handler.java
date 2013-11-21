/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.ArrayList;

import com.triniforce.postoffice.intf.LTRGetBoxes;


public class LTRGetBoxes_handler {
    static Object process(PostMaster pm, EnvelopeCtx ctx, Object data){
        
        LTRGetBoxes ltr = (LTRGetBoxes) data;
        Street ws = pm.m_rootStreet.queryPath(ltr.getStreetPath());
        return new ArrayList<String>(ws.getStreets().keySet());
        
    }

}
