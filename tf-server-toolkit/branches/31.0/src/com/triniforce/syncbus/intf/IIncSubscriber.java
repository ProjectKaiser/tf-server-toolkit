/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface IIncSubscriber extends ICSubscriber{
    SyncResponse onIncData(SyncMessage message);
}
