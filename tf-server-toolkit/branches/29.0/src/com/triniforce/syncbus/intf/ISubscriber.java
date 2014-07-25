/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ISubscriber{
    SubscrResponse handleEvent(Object addr, SubscrData data);  
}
