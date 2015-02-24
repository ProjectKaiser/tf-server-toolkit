/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.triniforce.db.test.TFTestCase;


public class ScheduledExecutorTest extends TFTestCase {
    
    public static class CountDownRunnable implements Runnable{
        
        private final CountDownLatch m_start, m_finish;

        public CountDownRunnable(CountDownLatch start, CountDownLatch finish) {
            m_start = start;
            m_finish = finish;
        }

        @Override
        public void run() {
            try {
                m_start.countDown();
                m_finish.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    
    public void test_fixedThreadPool() throws Exception {
        
        final CountDownLatch start = new CountDownLatch(3);
        final CountDownLatch finish = new CountDownLatch(1);
        
        
        ExecutorService es = Executors.newFixedThreadPool(2);
        Runnable r1 = new CountDownRunnable(start, finish);
        Runnable r2 = new CountDownRunnable(start, finish);
        Runnable r3 = new CountDownRunnable(start, finish);
        
        es.submit(r1);
        es.submit(r2);
        es.submit(r3);
        
        start.await();
        finish.countDown();
        
        trace("Completed");
        
    }

    
    @Override
    public void test() throws Exception {
    }

}
