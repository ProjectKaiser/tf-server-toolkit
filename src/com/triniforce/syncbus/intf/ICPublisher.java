/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ICPublisher {
    SyncVersionedData getCData();
    void stop();
}
