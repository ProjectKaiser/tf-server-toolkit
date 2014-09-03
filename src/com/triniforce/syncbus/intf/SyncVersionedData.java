/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.syncbus.intf;

public class SyncVersionedData {
    final long version;
    final Object data;

    public SyncVersionedData(long version, Object data) {
        this.version = version;
        this.data = data;
    }

}
