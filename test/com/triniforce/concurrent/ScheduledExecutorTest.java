/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.concurrent;

import java.util.concurrent.TimeUnit;

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
    
    
    public void testReport() throws InterruptedException{
    	
        ScheduledExecutor se = new ScheduledExecutor(2,3);
        for(int i=0; i<5; i++){
	        se.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}, 0, 500, TimeUnit.MILLISECONDS);
        }
        se.awaitTermination(200000L, TimeUnit.MILLISECONDS);
    }
    
    public void test2() throws InterruptedException{
    	ScheduledExecutor se = new ScheduledExecutor(2,3);
        assertNotNull(se.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
			}},5000 ,100, TimeUnit.MILLISECONDS));
        assertNotNull(se.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
			}},5000 ,400, TimeUnit.MILLISECONDS));
        assertNotNull(se.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
			}},5000 ,200, TimeUnit.MILLISECONDS));
        assertNotNull(se.scheduleWithFixedDelay(new Runnable(){
			@Override
			public void run() {
			}},5000 ,300, TimeUnit.MILLISECONDS));

        Thread.sleep(100);
        se.reportAboutTooBigDelay(new ScheduledExecutorTask(new Runnable(){
			@Override
			public void run() {
			}
        }, 0, 0), 100);
    }

}
