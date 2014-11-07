/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;


public interface ISyncBus {

    /**
     * If publisher with such name exists registration ignored
     * 
     * @param addr
     * @param cPub
     * @return
     */
    ICOffer registerCPublisher(Object addr, ICPublisher cPub);
    
    /**
     * 
     * Register using class name as addr
     * 
     */
    ICOffer registerCPublisher(ICPublisher cPub);
    
    IIncOffer registerIncPublisher(Object addr, IIncPublisher cPub);
    IIncOffer registerIncPublisher(IIncPublisher cPub);
    
    IShutdownStatus shutdownSubscriber(Object addr, ICSubscriber subscr);
//    
//    void postStop();
//    void waitForAllStoppedState();
    
    
    IPublisher queryPub(Object addr);
//    List<ISubscriber> querySubscribers(Object addr);
    
}
