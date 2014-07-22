/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

public interface IEntirePub {
    Object getEntireData();
    
    /** 
     * Called once in the beginning prior any getEntireData();
     */
    void setIEntireOffer(IEntireOffer ioffer);
    
    void stop();
}
