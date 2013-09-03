/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

import junit.framework.TestCase;

public class MessageBusTest extends TestCase{
    
    void checkLockCounts(MessageBus ns){
        assertEquals(0, ns.getRootLock().getWriteHoldCount());
        assertEquals(0, ns.getRootLock().getReadLockCount());
    }

    void checkConnected(MessageBus parent, MessageBus child, boolean connected){
    	if(connected){
    		assertSame(parent.getRootLock(), child.getRootLock());
    		assertSame(parent, child.getParent());
    		assertSame(parent.getPostMaster(), child.getPostMaster());
    	}else{
    		assertNotSame(parent.getRootLock(), child.getRootLock());
    		assertNotSame(parent, child.getParent());
    		assertNull(child.getPostMaster());
    	}
    }
    
    
    public void testBusConfigurationFields() throws Exception {
        MessageBus ns = new MessageBus();
        assertNull(ns.getParent());
        assertNotNull(ns.getRootLock());
        assertNull(ns.getPostMaster());

        assertEquals(MessageBus.BusStatus.NOT_STARTED, ns.getStatus());
        
        IPostMaster tempE = new IPostMaster() {
            public void enqueue(MessageBus srcNS, BusComponent srcComponent, BM bm) {
            	throw new IllegalArgumentException("Not supported");
            }
        };
        ns.m_postMaster = tempE;

        MessageBus ns1 = new MessageBus();
        //connect single namespace
        {
            assertNotSame(ns.getRootLock(), ns1.getRootLock());
            ns1.connectToParentBus(ns);
            checkLockCounts(ns);
            assertSame(ns.getRootLock(), ns1.getRootLock());
            assertSame(ns, ns1.getParent());
        }

        MessageBus ns2 = new MessageBus();
        MessageBus ns21 = new MessageBus();
        MessageBus ns22 = new MessageBus();
        //connect tree
        {
            checkConnected(ns, ns2, false);
            checkConnected(ns2, ns21, false);
            checkConnected(ns2, ns22, false);
            
            ns21.connectToParentBus(ns2);
            ns22.connectToParentBus(ns2);
            ns2.connectToParentBus(ns);
            
            checkConnected(ns, ns2, true);
            checkConnected(ns2, ns21, true);
            checkConnected(ns2, ns22, true);
            
        }
        //disconnect tree
        {
        	ns2.disconnectFromParentBus();
        	
            checkConnected(ns, ns2, false);
            checkConnected(ns2, ns21, true);
            checkConnected(ns2, ns22, true);

            assertNotNull(ns2.getRootLock());
            assertNotSame(ns2.getRootLock(), ns.getRootLock());
        }
        
    }

}
