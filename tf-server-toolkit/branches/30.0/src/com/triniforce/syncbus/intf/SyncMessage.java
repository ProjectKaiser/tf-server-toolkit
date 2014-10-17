/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public class SyncMessage {
    final Object addrFrom;
    final Object data;
    public SyncMessage(Object addrFrom, Object data) {
        this.addrFrom = addrFrom;
        this.data = data;
    }
}
