/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.ArrayList;

import com.triniforce.postoffice.intf.IEnvelopeCtx;
import com.triniforce.postoffice.intf.IOutbox;
import com.triniforce.postoffice.intf.LTRListStreets;

public class LTRListStreets_handler{
    static void process(PostMaster pm, IEnvelopeCtx ctx, LTRListStreets data, IOutbox out){
        Street ws = pm.m_rootStreet.queryPath(data.getStreetPath());
        
        out.reply(ctx.getEnvelope(), new ArrayList<String>(ws.getStreets().keySet()), null);
    }
}
