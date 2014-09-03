/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ICSubscriber{
    void stop();
    SyncResponse start();
    void onCData(SyncMessage message);
}
