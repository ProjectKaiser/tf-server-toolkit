/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ICSubscriber{
    void onCData(SyncMessage message);
}
