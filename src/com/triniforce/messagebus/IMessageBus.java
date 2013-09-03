/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

public interface IMessageBus {

    //Connecting to other buses
    void connectToParentBus(final MessageBus parent);
    void disconnectFromParentBus();
    
    //Registering components
    
    void registerComponent(IBusComponent e);
    void registerComponent(IBusComponent e, String name);
    void unregisterComponent(IBusComponent e);
    
    //Deliver message
    
    void deliverMessage(MessageBus srcBus, BusComponent srcComponent, BM bm);


}
