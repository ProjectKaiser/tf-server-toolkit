/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus.intf;

public interface ISyncBus {

    void registerEntirePub(Object addr, IEntirePub entPub);
    
    /**
     * @param entPub entPub.getClass().getName() will be used for addr
     */
    void registerEntirePub(IEntirePub entPub);
    
    void registerSubscriber(Object addr, ISubscriber subscr);

    /**
     * @param addr
     * @param subscr addr.getName() will be used as address
     */
    void registerSubscriber(Class addr, ISubscriber subscr);
    
    void unregisterSubscriber(ISubscriber subscr);
    void unregisterPub(ISubscriber subscr);

    
    void postStop();
    void waitForAllStoppedState();
    
    void postPub(Object addrFrom, SDPub pubData);
    
    IEntirePub queryPub(Object addr);
    
    
    /**
     * @param url
     * @param incThreshold If subscriber queue exceeds this value it receives ENTIRE notification
     * @return
     */

    //void registerIncPub(Object url, IEntirePub incPub, int incThreshold);
    
}
