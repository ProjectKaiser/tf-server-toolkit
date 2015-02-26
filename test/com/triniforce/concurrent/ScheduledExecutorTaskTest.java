/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

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

    ScheduledExecutorTask newStartedTask(long initialDelayMs, long delayMs){
        ScheduledExecutorTask res = newTask(initialDelayMs, delayMs);
        res.calcNextStart();
        return res;
    }

    
    ScheduledExecutorTask newTask(long initialDelayMs, long delayMs){
        ScheduledExecutorTask res = new ScheduledExecutorTask(null, initialDelayMs, delayMs);
        res.setTime(new ITime(){

            @Override
            public long currentTimeMillis() {

                return m_time;
            }});
        return res;
    }
    
    public void test_queue() throws InterruptedException{
        
        {
            ScheduledExecutorTask t1 = newStartedTask(1, 2);
            ScheduledExecutorTask t11 = newStartedTask(11, 2);
            ScheduledExecutorTask t2 = newStartedTask(2, 2);
            ScheduledExecutorTask t3 = newStartedTask(3, 2);
            PriorityBlockingQueue<ScheduledExecutorTask> q = new PriorityBlockingQueue<ScheduledExecutorTask>();
            
            q.add(t3);
            q.add(t2);
            q.add(t1);
            q.add(t11);
            assertSame(t1, q.poll(10, TimeUnit.HOURS));
            assertSame(t2, q.poll(10, TimeUnit.HOURS));
            assertSame(t3, q.poll(10, TimeUnit.HOURS));
            assertSame(t11, q.poll(10, TimeUnit.HOURS));
        }
        
        {
            ScheduledExecutorTask t1 = newStartedTask(1, 2);
            ScheduledExecutorTask t2 = newStartedTask(2, 2);
            ScheduledExecutorTask t3 = newStartedTask(3, 2);
            PriorityBlockingQueue<ScheduledExecutorTask> q = new PriorityBlockingQueue<ScheduledExecutorTask>();
            
            q.add(t2);
            q.add(t1);
            assertSame(t1, q.poll());
            q.add(t3);
            assertSame(t2, q.poll());
        }
        
        
        
    }
    
    public void testDelay(){
        m_time = 0;
        ScheduledExecutorTask t1 = newTask(1, 2);
        t1.calcNextStart();
        assertEquals(1, t1.getDelay(TimeUnit.MILLISECONDS));
        t1.calcNextStart();
        assertEquals(2, t1.getDelay(TimeUnit.MILLISECONDS));
        assertEquals(2000, t1.getDelay(TimeUnit.MICROSECONDS));
    }
    
    /**
     * Test constructor, calcNextStart and Comparable
     */
    @Override
    public void test() throws Exception {
        m_time = 0;
        
        ScheduledExecutorTask t1 = newTask(1, 2);
        t1.calcNextStart();
        ScheduledExecutorTask t2 = newTask(4, 5);
        t2.calcNextStart();
        ScheduledExecutorTask t3 = newTask(4, 1);
        t3.calcNextStart();

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
            ScheduledExecutorTask runOnce = newTask(7, 0);
            assertTrue(runOnce.calcNextStart());
            assertFalse(runOnce.calcNextStart());
            
            ScheduledExecutorTask runOnce2 = newTask(7, -2);
            assertTrue(runOnce2.calcNextStart());
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
