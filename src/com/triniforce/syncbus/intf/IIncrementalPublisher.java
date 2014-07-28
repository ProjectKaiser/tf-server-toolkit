/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface IIncrementalPublisher extends ICompletePublisher{ 
    
    /**
     * Called once in the beginning prior any getEntireData();
     */
    void setIIncOffer(ICompleteOffer ioffer);


}
