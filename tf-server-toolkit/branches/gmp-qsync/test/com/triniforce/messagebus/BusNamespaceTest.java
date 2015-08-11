/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

import junit.framework.TestCase;

public class BusNamespaceTest extends TestCase{
    
    void checkLockCounts(BusNamespace ns){
        assertEquals(0, ns.getRootLock().getWriteHoldCount());
        assertEquals(0, ns.getRootLock().getReadLockCount());
    }

    void checkConnected(BusNamespace parent, BusNamespace child, boolean connected){
    	if(connected){
    		assertSame(parent.getRootLock(), child.getRootLock());
    		assertSame(parent, child.getParent());
    		assertSame(parent.getIEnqueueBM(), child.getIEnqueueBM());
    	}else{
    		assertNotSame(parent.getRootLock(), child.getRootLock());
    		assertNotSame(parent, child.getParent());
    		assertNull(child.getIEnqueueBM());
    	}
    }
    
    
    public void testBusConfigurationFields() throws Exception {
        BusNamespace ns = new BusNamespace();
        assertNull(ns.getParent());
        assertNotNull(ns.getRootLock());
        assertNull(ns.getIEnqueueBM());

        assertEquals(BusNamespace.BusStatus.NOT_STARTED, ns.getStatus());
        
        IEnqueueBM tempE = new IEnqueueBM() {
            public void enqueue(BusNamespace srcNS, BusComponent srcComponent, BM bm) {
            	throw new IllegalArgumentException("Not supported");
            }
        };
        ns.m_IEnqueueBM = tempE;

        BusNamespace ns1 = new BusNamespace();
        //connect single namespace
        {
            assertNotSame(ns.getRootLock(), ns1.getRootLock());
            ns1.connect(ns);
            checkLockCounts(ns);
            assertSame(ns.getRootLock(), ns1.getRootLock());
            assertSame(ns, ns1.getParent());
        }

        BusNamespace ns2 = new BusNamespace();
        BusNamespace ns21 = new BusNamespace();
        BusNamespace ns22 = new BusNamespace();
        //connect tree
        {
            checkConnected(ns, ns2, false);
            checkConnected(ns2, ns21, false);
            checkConnected(ns2, ns22, false);
            
            ns21.connect(ns2);
            ns22.connect(ns2);
            ns2.connect(ns);
            
            checkConnected(ns, ns2, true);
            checkConnected(ns2, ns21, true);
            checkConnected(ns2, ns22, true);
            
        }
        //disconnect tree
        {
        	ns2.disconnect();
        	
            checkConnected(ns, ns2, false);
            checkConnected(ns2, ns21, true);
            checkConnected(ns2, ns22, true);

            assertNotNull(ns2.getRootLock());
            assertNotSame(ns2.getRootLock(), ns.getRootLock());
        }
        
    }

}
