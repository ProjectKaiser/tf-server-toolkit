/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.syncbus;

import java.util.concurrent.ConcurrentHashMap;

import com.triniforce.syncbus.intf.ICompleteOffer;
import com.triniforce.syncbus.intf.ICompletePublisher;
import com.triniforce.syncbus.intf.ISubscriber;
import com.triniforce.syncbus.intf.ISyncBus;
import com.triniforce.syncbus.intf.SDEntire;

public class SyncBus implements ISyncBus{
    
    class EntireOffer implements ICompleteOffer{
        
        private final Object addr;
        public EntireOffer(Object addr) {
            super();
            this.addr = addr;
        }
        public boolean entireOffer(Object o){
            return entireOfferWithAddr(addr, o);
        }
       
    }
    
    ConcurrentHashMap<Object, PubHolder> m_pubs = new ConcurrentHashMap<Object, PubHolder>();
    
    boolean entireOfferWithAddr(Object addr, Object data){
        return true;
    }
    
    
    public void registerEntirePub(Object addr, ICompletePublisher ep) {
        ICompleteOffer eo = new EntireOffer(addr);
        PubHolder h = new PubHolder(addr, ep, eo);
        PubHolder old = m_pubs.putIfAbsent(addr, h);
        if(null == old){
            h.getEp().setIEntireOffer(eo);
        }
    }

    public void registerEntirePub(ICompletePublisher entPub) {
        registerEntirePub(entPub.getClass().getName(), entPub);
        
    }

    public void registerSubscriber(Object addr, ISubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void registerSubscriber(Class addr, ISubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void unregisterSubscriber(ISubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void unregisterPub(ISubscriber subscr) {
        // TODO Auto-generated method stub
        
    }

    public void postStop() {
        // TODO Auto-generated method stub
        
    }

    public void waitForAllStoppedState() {
        // TODO Auto-generated method stub
        
    }

    public void postPub(Object addrFrom, SDEntire pubData) {
        // TODO Auto-generated method stub
        
    }

    public ICompletePublisher queryPub(Object addr) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
