/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface IIncPub extends IEntirePub{ 
    
    /**
     * Called once in the beginning prior any getEntireData();
     */
    void setIIncOffer(IEntireOffer ioffer);


}
