/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface IShutdownStatus {
    boolean awaitTermination(long milliseconds);
}
