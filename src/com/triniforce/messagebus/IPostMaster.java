/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

public interface IPostMaster {
    void enqueue(MessageBus srcBus, BusComponent srcComponent, BM bm);
}
