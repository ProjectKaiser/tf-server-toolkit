/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ICSubscriber extends ISubscriber{
    void onCData(SyncMessage message);
}
