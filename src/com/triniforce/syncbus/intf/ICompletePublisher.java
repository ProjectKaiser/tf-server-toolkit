/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ICompletePublisher {
    Object getEntireData();
    
    /** 
     * Called once in the beginning prior any getEntireData();
     */
    void setIEntireOffer(ICompleteOffer ioffer);
    
    void stop();
}
