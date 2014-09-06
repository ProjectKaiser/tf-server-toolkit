/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ISubscriber {
    void stop(Object addr);
    SyncResponse start();
}
