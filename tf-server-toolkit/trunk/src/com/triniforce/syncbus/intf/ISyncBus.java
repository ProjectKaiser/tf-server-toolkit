/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ISyncBus {

    void registerCPublisher(Object addr, ICPublisher cPub);
    
    /**
     * @param entPub entPub.getClass().getName() will be used for addr
     */
    void registerCPublisher(ICPublisher cPub);
    
    void registerSubscriber(Object addr, ICSubscriber subscr);
    void registerIncSubscriber(Object addr, IIncSubscriber subscr);

    void unregisterSubscriber(ICSubscriber subscr);
    void unregisterPub(ICSubscriber subscr);

    
    void postStop();
    void waitForAllStoppedState();
    
    
    ICPublisher queryPub(Object addr);
    
    
    /**
     * @param url
     * @param incThreshold If subscriber queue exceeds this value it receives ENTIRE notification
     * @return
     */

    //void registerIncPub(Object url, IEntirePub incPub, int incThreshold);
    
}
