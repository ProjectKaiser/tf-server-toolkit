/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import java.util.concurrent.ExecutionException;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class ScheduledExecutorTaskTest extends TFTestCase {
    
    long m_time= 0;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ITime t = new ITime(){
            @Override
            public long currentTimeMillis() {
                return m_time;
            }
        };
        ApiStack.pushInterface(ITime.class, t);
    }
    
    @Override
    protected void tearDown() throws Exception {
        ApiStack.popInterface(1);
        super.tearDown();
    }
    
    /**
     * Test constructor, calcNextStart and Comparable
     */
    @Override
    public void test() throws Exception {
        m_time = 0;
        
        ScheduledExecutorTask t1 = new ScheduledExecutorTask(null, 1, 2);
        ScheduledExecutorTask t2 = new ScheduledExecutorTask(null, 4, 5);
        ScheduledExecutorTask t3 = new ScheduledExecutorTask(null, 4, 1);

        //1 < 4
        assertTrue(t1.compareTo(t2) < 0);
        //4 == 5
        assertTrue(t2.compareTo(t3) == 0);
        //4 > 1
        assertTrue(t3.compareTo(t1) > 0);

        m_time = 1;
        t1.calcNextStart();// 1 + 2
        // 1 + 2 < 4
        assertTrue(t1.compareTo(t2) < 0);
        
        m_time = 3;
        t1.calcNextStart();// 3 + 2
        // 3 + 2 > 4
        assertTrue(t1.compareTo(t2) > 0);

        //run-once task
        {
            ScheduledExecutorTask runOnce = new ScheduledExecutorTask(null, 7, 0);
            assertFalse(runOnce.calcNextStart());
            
            ScheduledExecutorTask runOnce2 = new ScheduledExecutorTask(null, 7, -2);
            assertFalse(runOnce2.calcNextStart());
        }

    }
    
    public void testGet() throws InterruptedException, ExecutionException{
        ScheduledExecutorTask t1 = new ScheduledExecutorTask(null, 1, 2);
        assertNull(t1.get());
    }
    
    public void testCancel(){
        ScheduledExecutorTask t1 = new ScheduledExecutorTask(null, 1, 2);
        assertTrue(t1.calcNextStart());
        assertTrue(t1.calcNextStart());
        assertTrue(t1.calcNextStart());
        assertFalse(t1.isDone());
        assertFalse(t1.isCancelled());
        
        t1.cancel(true);
        
        assertFalse(t1.calcNextStart());
        assertTrue(t1.isDone());
        assertTrue(t1.isCancelled());
    }

}
