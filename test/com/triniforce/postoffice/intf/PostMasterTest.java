/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.intf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.triniforce.db.test.TFTestCase;

public class PostMasterTest extends TFTestCase {

    //Test that submit is not blocked
    @Override
    public void test() throws Exception {
        
        final Lock lock = new ReentrantLock();
        
        lock.lock();

        ExecutorService es = Executors.newFixedThreadPool(1);
        for(int i = 0; i< 10; i++){
            es.submit(new Runnable() {
                
                public void run() {
                    lock.lock();
                    lock.unlock();
                }
            });
            
        }
        lock.unlock();
        es.shutdownNow();
        es.awaitTermination(10000, TimeUnit.SECONDS);
        
    }

}
