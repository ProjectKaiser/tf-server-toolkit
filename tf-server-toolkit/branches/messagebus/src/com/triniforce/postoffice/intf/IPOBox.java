/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

public interface IPOBox extends IEnvelopeHandler{
    
    /**
     * 
     * Called prior to any process() call 
     * @param out
     * 
     */
    void priorProcess(IOutbox out);
    
    /**
     * 
     * Called asynchronously, intervalMs before call to onDisconnect(), should return immediately
     * 
     * /
    void beforeDisconnect(int intervalMs);
    /**
     * Called asynchronously, should return immediately
     */
    void onDisconnect();
}
