/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

public interface IPOBox extends IEnvelopeHandler{
    /**
     * Called asynchronously, should return immediately
     */
    void beforeDisconnect(int intervalMs);
    /**
     * Called asynchronously, should return immediately
     */
    void onDisconnect();
}
