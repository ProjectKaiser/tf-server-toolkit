/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

public interface ISyncBus {
    void publish(Object url, Object data);
    //void subscribe(Object url, ISubscribe s);
}
