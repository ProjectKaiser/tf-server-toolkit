/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.triniforce.db.test.TFTestCase;

public class InvThreadExecutorsTest extends TFTestCase {

    // new ThreadPoolExecutor(2, MAX_NORMAL_THREADS, 10, TimeUnit.SECONDS
    // , new SynchronousQueue<Runnable>())

    public static class CountDownRunnable implements Runnable {

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

        final CountDownLatch start1_2 = new CountDownLatch(2);
        final CountDownLatch start3 = new CountDownLatch(2);
        final CountDownLatch finish = new CountDownLatch(1);

        ExecutorService es = Executors.newFixedThreadPool(2);
        Runnable r1 = new CountDownRunnable(start1_2, finish);
        Runnable r2 = new CountDownRunnable(start1_2, finish);
        Runnable r3 = new CountDownRunnable(start3, finish);

        es.submit(r1);
        es.submit(r2);
        start1_2.await();
        trace("1 & 2 started");

        // no exception
        es.submit(r3);

        finish.countDown();

        trace("Completed");

    }

    //Test that SynchronousQueue gives RejectedExecutionException
    public void test() throws Exception {

        final CountDownLatch start1_2 = new CountDownLatch(2);
        final CountDownLatch start3 = new CountDownLatch(2);
        final CountDownLatch finish = new CountDownLatch(1);

        ExecutorService es = new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
        Runnable r1 = new CountDownRunnable(start1_2, finish);
        Runnable r2 = new CountDownRunnable(start1_2, finish);
        Runnable r3 = new CountDownRunnable(start3, finish);

        es.submit(r1);
        es.submit(r2);
        start1_2.await();
        trace("1 & 2 started");

        try {
            es.submit(r3);
            fail();
        } catch (RejectedExecutionException e) {
        }

        finish.countDown();
        trace("Completed");
    }

}
