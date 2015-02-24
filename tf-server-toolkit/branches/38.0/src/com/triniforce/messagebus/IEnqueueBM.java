/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

public interface IEnqueueBM {
    void enqueue(BusNamespace srcNS, BusComponent srcComponent, BM bm);
}
