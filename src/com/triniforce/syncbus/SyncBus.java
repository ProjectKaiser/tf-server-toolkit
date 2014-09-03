/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import com.triniforce.syncbus.intf.ICPublisher;
import com.triniforce.syncbus.intf.ICSubscriber;
import com.triniforce.syncbus.intf.IIncSubscriber;
import com.triniforce.syncbus.intf.ISyncBus;

public class SyncBus implements ISyncBus{

    public void registerEntirePub(Object addr, ICPublisher entPub) {
        // TODO Auto-generated method stub
        
    }

    public void registerEntirePub(ICPublisher entPub) {
        // TODO Auto-generated method stub
        
    }

    public void registerSubscriber(Object addr, ICSubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void registerSubscriber(Class addr, ICSubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void unregisterSubscriber(ICSubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void unregisterPub(ICSubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void postStop() {
        // TODO Auto-generated method stub
        
    }

    public void waitForAllStoppedState() {
        // TODO Auto-generated method stub
        
    }


    public ICPublisher queryPub(Object addr) {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerCPublisher(Object addr, ICPublisher cPub) {
        // TODO Auto-generated method stub
        
    }

    public void registerCPublisher(ICPublisher cPub) {
        // TODO Auto-generated method stub
        
    }

    public void registerIncSubscriber(Object addr, IIncSubscriber subscr) {
        // TODO Auto-generated method stub
        
    }
}
