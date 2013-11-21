/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.ArrayList;

import com.triniforce.postoffice.intf.LTRGetStreets;

public class LTRGetStreets_handler {
    static Object process(PostMaster pm, EnvelopeCtx ctx, Object data){
        LTRGetStreets ltr = (LTRGetStreets) data;
        Street ws = pm.m_rootStreet.queryPath(ltr.getStreetPath());
        return new ArrayList<String>(ws.getStreets().keySet());
    }
}
