/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.postoffice.intf.IPostMaster;
import com.triniforce.postoffice.intf.LTRGetStreets;

public class PostMasterTest extends TFTestCase {

    IPostMaster pm;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pm = new PostMaster();
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        pm.stop(1000);
        super.tearDown();
    }
    
    public void testStreets() throws InterruptedException, ExecutionException{

        //empty root
        {
            Future f = pm.post(null, null, new LTRGetStreets());
            List<String> res = (List<String>) f.get();
            assertEquals(0, res.size());
        }
        
        //add empty street
        {
            Future f = pm.post(null, null, new LTRGetStreets());
            List<String> res = (List<String>) f.get();
            assertEquals(0, res.size());
        }
        
    }
    
    //Test that submit is not blocked
    @Override
    public void test() throws Exception{
        
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
