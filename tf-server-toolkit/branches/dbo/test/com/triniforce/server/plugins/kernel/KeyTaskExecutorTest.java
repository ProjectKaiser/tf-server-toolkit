/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.plugins.kernel;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.ICheckInterrupted;
import com.triniforce.utils.EUtils.EAssertionFailed;

public class KeyTaskExecutorTest extends TFTestCase {

    /**
     * Tasks list is shared between execut
     */
    public void testSharedTaskList() throws Exception{
        KeyTaskExecutor te1 = new KeyTaskExecutor(2, 2);
        KeyTaskExecutor te2 = new KeyTaskExecutor(2, 2);
        
        final SynchronousQueue<String> q1 = new SynchronousQueue<String>();
        
        Runnable r = new Runnable() {
            public void run() {
                try {
                    trace(q1.take());
                    q1.put("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        
        te1.execute(r);
        q1.put("te1 started");
        te2.execute(r);
        while(te2.getRejectedCount()==0){
            Thread.sleep(1000);
        }
        
    }
    
    public void testNThreads() throws InterruptedException {

        final int coreSize = 4;
        final int nThreads = coreSize + 5;

        KeyTaskExecutor te1 = new KeyTaskExecutor(coreSize, nThreads);
        KeyTaskExecutor te2 = new KeyTaskExecutor(coreSize, nThreads);

        final LinkedBlockingQueue qr1 = new LinkedBlockingQueue();
        final LinkedBlockingQueue qr2 = new LinkedBlockingQueue();
        
        final SynchronousQueue<String> qw1 = new SynchronousQueue<String>();        
        final SynchronousQueue<String> qw2 = new SynchronousQueue<String>();        
        // final List<SynchronousQueue> qs = new ArrayList<SynchronousQueue>();
        for (int i = 0; i < nThreads; i++) {
            qr1.put(Integer.toString(i));
            qr2.put(Integer.toString(i));
        }

        for (int i = 0; i < nThreads; i++) {
            Runnable r1 = new Runnable() {
                public void run() {
                    try {
                        trace(qr1.take());
                        qw1.put("");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            te1.execute(r1);
            Runnable r2 = new Runnable() {
                public void run() {
                    try {
                        trace(qw1.take());
                        qw2.put("");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            te2.execute(r2);            
        }

        while (qr1.size() > 0) {
            ICheckInterrupted.Helper.sleep(1000);
        }
        while (qw1.size() > 0) {
            ICheckInterrupted.Helper.sleep(1000);
        }        
        for (int i = 0; i < nThreads; i++) {
            qr2.take();
            qw2.take();
        }

    }

    @Override
    public void test() throws Exception {
        {
            assertEquals(2, new KeyTaskExecutor(2, 4).getCorePoolSize());
            assertEquals(154, new KeyTaskExecutor(154, 240).getCorePoolSize());
        }
        try {
            new KeyTaskExecutor(0);
            fail();
        } catch (EAssertionFailed e) {
            trace(e);
        }
        try {
            new KeyTaskExecutor(-1);
            fail();
        } catch (EAssertionFailed e) {
            trace(e);
        }

        {
            final SynchronousQueue sq11 = new SynchronousQueue();
            final SynchronousQueue sq12 = new SynchronousQueue();
            KeyTaskExecutor e = new KeyTaskExecutor(2);
            try {
                Runnable r1 = new Runnable() {
                    public void run() {
                        try {
                            trace("Hello");
                            sq11.put("s");
                            sq12.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                e.execute(r1);
                sq11.take();
                assertEquals(0, e.getRejectedCount());
                e.execute(r1);
                while (e.getRejectedCount() != 1) {
                    Thread.sleep(100);
                }

                final SynchronousQueue sq21 = new SynchronousQueue();
                final SynchronousQueue sq22 = new SynchronousQueue();
                Runnable r2 = new Runnable() {
                    public void run() {
                        try {
                            trace("Hello21");
                            sq21.put("s");
                            sq22.take();
                            trace("Hello22");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                e.execute(r2);
                sq21.take();

                final SynchronousQueue sq31 = new SynchronousQueue();
                final SynchronousQueue sq32 = new SynchronousQueue();
                Runnable r3 = new Runnable() {
                    public void run() {
                        trace("Hello31");
                        try {
                            sq31.put("s");
                            sq32.take();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                };

                assertEquals(1, e.getRejectedCount());
                e.execute(r3);
                // r3 will be rejected since only 2 threads allowed
                while (e.getRejectedCount() != 2) {
                    Thread.sleep(100);
                }

                // stops first two threads
                sq12.put("");
                while (e.getActiveCount() != 1) {
                    Thread.sleep(100);
                }

                // put r1 again
                assertEquals(2, e.getRejectedCount());
                e.execute(r1);
                sq11.take();

                sq12.put("");
                sq22.put("");

            } finally {
                e.shutdown();
                e.awaitTermination(600, TimeUnit.SECONDS);
            }
            assertEquals(2, e.getRejectedCount());
            assertEquals(3, e.getFinishedCount());

        }
    }
}