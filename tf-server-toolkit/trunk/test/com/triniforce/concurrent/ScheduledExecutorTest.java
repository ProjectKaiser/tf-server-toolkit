/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import com.triniforce.db.test.TFTestCase;


public class ScheduledExecutorTest extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        
        // +1 to counts
        ScheduledExecutor se = new ScheduledExecutor(2,3);
        assertEquals(se.getCorePoolSize(), 3);
        assertEquals(se.getMaximumPoolSize(), 4);
        
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                trace(Thread.currentThread());
                assertTrue(Thread.currentThread().getName().endsWith("_2"));
            }
        };
        
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                assertTrue(Thread.currentThread().getName().endsWith("_3"));
            }
        };
        
        se.submit(r1).get();
        se.submit(r2).get();
        
    }

}
