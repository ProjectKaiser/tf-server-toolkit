/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface IIncrementalOffer extends ICompleteOffer{
    void incOffer(Object data);
    
}
