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
    ICOffer registerCPublisher(ICPublisher cPub);
    
    IIncOffer registerIncPublisher(Object addr, IIncPublisher cPub);
    IIncOffer registerIncPublisher(IIncPublisher cPub);
    
    
//    void registerSubscriber(Object addr, ISubscriber subscr) throws EInvalidSubscriberType, ENoSuchPublisher;
//    void unregisterSubscriber(Object addr, ICSubscriber subscr);
//    
//    void postStop();
//    void waitForAllStoppedState();
    
    
    IPublisher queryPub(Object addr);
//    List<ISubscriber> querySubscribers(Object addr);
    
}
