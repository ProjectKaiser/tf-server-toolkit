/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.ICheckInterrupted.EInterrupted;

import junit.framework.TestCase;

public class ICheckInterruptedTest extends TFTestCase {

    public void testIsInterruptedException() {
        assertTrue(ICheckInterrupted.Helper
                .isInterruptedException(new InterruptedException()));
    }

    static class TestThread extends Thread {
        boolean ok = false;

        TestCase tc;

        Thread m_thread;

        LogFactory logFactory;

        public void test() {
        }

        @Override
        public void run() {
            ok = false;
            try {
                Api api = new Api();
                api.setIntfImplementor(ICheckInterrupted.class,
                        new ICheckInterrupted.CheckInterrupted());
                api.setIntfImplementor(LogFactory.class, logFactory);
                ApiStack.pushApi(api);
                try {
                    test();
                    ok = true;
                } finally {
                    ApiStack.popApi();
                }
            } catch (Throwable t) {
            }
        }
    }

    static class CheckInterruptedAndSleepThread extends TestThread {
        @Override
        public void test() {
            ICheckInterrupted.Helper.checkInterrupted();
            ICheckInterrupted.Helper.sleep(0);
            TestCase.assertFalse(Thread.currentThread().isInterrupted());
            m_thread.interrupt();
            TestCase.assertTrue(Thread.currentThread().isInterrupted());
            try {
                ICheckInterrupted.Helper.checkInterrupted();
                TestCase.fail();
            } catch (EInterrupted e) {
            }
            TestCase.assertTrue(Thread.currentThread().isInterrupted());
            try {
                ICheckInterrupted.Helper.sleep(10);
                TestCase.fail();
            } catch (EInterrupted e) {
            }
            TestCase.assertTrue(Thread.currentThread().isInterrupted());
            try {
                ICheckInterrupted.Helper.sleep(10);
                TestCase.fail();
            } catch (EInterrupted e) {
            }
            TestCase.assertTrue(Thread.currentThread().isInterrupted());
        }
    }

    static class CheckSleepThread extends TestThread {
        @Override
        public void test() {
            ICheckInterrupted.Helper.checkInterrupted();
            ICheckInterrupted.Helper.sleep(0);
            m_thread.interrupt();
            try {
                ICheckInterrupted.Helper.sleep(10);
                TestCase.fail();
            } catch (EInterrupted e) {
            }
            try {
                ICheckInterrupted.Helper.sleep(10);
                TestCase.fail();
            } catch (EInterrupted e) {
            }
            TestCase.assertTrue(Thread.currentThread().isInterrupted());
        }
    }

    static class CheckLoggerThread extends TestThread {
        @Override
        public void test() {
            m_thread.interrupt();
            try {
                ICheckInterrupted.Helper.sleep(10);
                TestCase.fail();
            } catch (EInterrupted e) {
                LogFactory lf = ApiStack.getInterface(LogFactory.class); 
                Log log = lf.getInstance(CheckLoggerThread.class);
                log.trace(
                        Thread.currentThread().isInterrupted());
                log.trace(
                        Thread.currentThread().isInterrupted());
                if (!Thread.currentThread().isInterrupted()) {
                    ApiAlgs.getLog(this).trace("Logger FAILED");
                } else {
                    ApiAlgs.getLog(this).trace("Logger is fine");
                }
            }
        }
    }

    protected void runTestThread(Class cls) throws Exception {
        TestThread t = (TestThread) cls.newInstance();
        t.tc = this;
        t.m_thread = t;
        t.logFactory = ApiStack.getInterface(LogFactory.class);
        t.start();
        t.join();
        assertTrue(t.ok);
    }

    public void testIsInterruptedAndSleep() throws Exception {
        runTestThread(CheckInterruptedAndSleepThread.class);
        runTestThread(CheckSleepThread.class);
    }

    public void testLogger() throws Exception {
        ApiAlgs.getLog(this).trace("Hi");
        runTestThread(CheckLoggerThread.class);
    }

}
