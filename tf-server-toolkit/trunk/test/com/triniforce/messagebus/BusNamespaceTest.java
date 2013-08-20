/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

import com.triniforce.db.test.TFTestCase;

public class BusNamespaceTest extends TFTestCase{
    
    void checkLockCounts(BusNamespace ns){
        assertEquals(0, ns.getRootLock().getWriteHoldCount());
        assertEquals(0, ns.getRootLock().getReadLockCount());
    }
    
    
    @Override
    public void test() throws Exception {
        BusNamespace ns = new BusNamespace();
        assertNull(ns.getIEnqueueBM());
        assertEquals(BusNamespace.BusStatus.NOT_STARTED, ns.getStatus());

        //connect single namespace
        {
            BusNamespace ns1 = new BusNamespace();
            assertNotSame(ns.getRootLock(), ns1.getRootLock());
            ns1.connect(ns);
            checkLockCounts(ns);
            assertSame(ns.getRootLock(), ns1.getRootLock());
        }
        //connect tree
        {
            IEnqueueBM tempE = new IEnqueueBM() {
                public void enqueue(BusNamespace srcNS, BusComponent srcComponent, BM bm) {
                }
            };
            ns.m_IEnqueueBM = tempE;
            
            BusNamespace ns2 = new BusNamespace();
            BusNamespace ns21 = new BusNamespace();
            BusNamespace ns22 = new BusNamespace();
            assertNotSame(ns.getRootLock(), ns2.getRootLock());
            assertNotSame(ns.getRootLock(), ns21.getRootLock());
            assertNotSame(ns.getRootLock(), ns22.getRootLock());
            assertNotSame(ns.getIEnqueueBM(), ns22.getIEnqueueBM());
            ns21.connect(ns2);
            ns22.connect(ns2);
            ns2.connect(ns);
            assertSame(ns.getRootLock(), ns2.getRootLock());
            assertSame(ns.getRootLock(), ns21.getRootLock());
            assertSame(ns.getRootLock(), ns22.getRootLock());
            assertSame(ns.getIEnqueueBM(), ns22.getIEnqueueBM());
        }
        
    }

}
