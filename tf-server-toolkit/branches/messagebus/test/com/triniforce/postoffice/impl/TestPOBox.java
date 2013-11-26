/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.IEnvelopeCtx;
import com.triniforce.postoffice.intf.IOutbox;
import com.triniforce.postoffice.intf.IPOBox;

public class TestPOBox implements IPOBox{

    public void process(IEnvelopeCtx ctx, Object data, IOutbox out) {
        
    }

    public void beforeDisconnect(int intervalMs) {
    }

    public void onDisconnect() {
    }

    public void priorProcess(IOutbox out) {
    }

}
