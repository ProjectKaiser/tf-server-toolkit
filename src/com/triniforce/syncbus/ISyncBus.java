/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

public interface ISyncBus {

    void registerEntirePub(Object addr, IEntirePub entPub);
    
    /**
     * @param entPub entPub.getClass().getName() will be used for addr
     */
    void registerEntirePub(IEntirePub entPub);
    
    void registerSubscriber(Object addr, ISubscriber subscr);
    
    void unregisterSubscriber(ISubscriber subscr);
    void unregisterPub(ISubscriber subscr);

    
    void postStop();
    void waitForStoppedState();
    
    /**
     * @param url
     * @param incThreshold If subscriber queue exceeds this value it receives ENTIRE notification
     * @return
     */

    //void registerIncPub(Object url, IEntirePub incPub, int incThreshold);
    
}
