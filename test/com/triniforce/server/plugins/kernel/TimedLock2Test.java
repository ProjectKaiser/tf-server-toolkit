/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.ITimedLock2;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class TimedLock2Test extends BasicServerTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getServer().enterMode(Mode.Running);
        
    }
    
    @Override
    protected void tearDown() throws Exception {
    	getServer().leaveMode();
        super.tearDown();
    }
    /*
     * Test constructor
     */
    public void test() {
        ITimedLock2 tl = new TimedLock2();
        assertEquals(0, tl.getCurrentTimestamp());
        assertEquals(10000, tl.getTimeout());
        assertEquals(null, tl.getCurrentCB());
        assertTrue(tl.isAvailable());
    }

    ITimedLock2.ITimedLockCB m_cb1 = new ITimedLock2.ITimedLockCB() {
        public void unlocked() {

        }
    };

    ITimedLock2.ITimedLockCB m_cb2 = new ITimedLock2.ITimedLockCB() {
        public void unlocked() {

        }
    };

    public void testCheckTimeout() {
        Mockery ctx = new Mockery();
        final ITime t = ctx.mock(ITime.class);
        ApiStack.pushInterface(ITime.class, t);
        try {
            {// non initialized
                ITimedLock2 tl = new TimedLock2();
                tl.checkTimeout();
                assertEquals(true, tl.isAvailable());
                assertEquals(null, tl.getCurrentCB());
                assertEquals(0L, tl.getCurrentTimestamp());
            }
            {// no timeout
                ctx.checking(new Expectations() {
                    {
                        exactly(1).of(t).currentTimeMillis();will(returnValue(1969L));
                        exactly(1).of(t).currentTimeMillis();will(returnValue(1969L));
                    }
                });
                final ITimedLock2.ITimedLockCB cb = ctx.mock(ITimedLock2.ITimedLockCB.class);
                ITimedLock2 tl = new TimedLock2();
                tl.acquireLock(cb);
                tl.checkTimeout();
                assertEquals(false, tl.isAvailable());
                assertEquals(cb, tl.getCurrentCB());
                assertEquals(1969L, tl.getCurrentTimestamp());
                tl.releaseLock(cb);
            }
            {// timeout
                ITimedLock2 tl = new TimedLock2();
                final ITimedLock2.ITimedLockCB cb = ctx.mock(ITimedLock2.ITimedLockCB.class, "timeoutcb");
                final long start = 1000;
                final long cur = start + tl.getTimeout() + 10;                
                ctx.checking(new Expectations() {
                    {
                        exactly(1).of(t).currentTimeMillis();will(returnValue(start));
                        exactly(1).of(t).currentTimeMillis();will(returnValue(cur));
                        exactly(1).of(cb).unlocked();
                    }
                });
                tl.acquireLock(cb);
                tl.checkTimeout();
                ctx.assertIsSatisfied();
                assertEquals(true, tl.isAvailable());
                assertEquals(null, tl.getCurrentCB());
                assertEquals(0L, tl.getCurrentTimestamp());
            }            
        } finally {
            ApiStack.popInterface(1);
        }
    }

    // @SuppressWarnings("unused")
    public void testAcquireRelease() {
        Mockery ctx = new Mockery();
        final ITime t = ctx.mock(ITime.class);
        ApiStack.pushInterface(ITime.class, t);
        try {
            // simple acquire
            {
                ctx.checking(new Expectations() {
                    {
                        exactly(1).of(t).currentTimeMillis();will(returnValue(1969L));
                    }
                });

                ITimedLock2 tl = new TimedLock2();
                tl.acquireLock(m_cb1);

                assertFalse(tl.isAvailable());
                assertEquals(m_cb1, tl.getCurrentCB());
                assertEquals(1969L, tl.getCurrentTimestamp());
                assertEquals(Thread.currentThread(), tl.getLockerThread());

                tl.releaseLock(m_cb1);

                assertTrue(tl.isAvailable());
                assertEquals(null, tl.getCurrentCB());
                assertEquals(null, tl.getLockerThread());
                assertEquals(0L, tl.getCurrentTimestamp());

            }
            //double acquire
            {
                ctx.checking(new Expectations() {
                    {
                        exactly(1).of(t).currentTimeMillis();will(returnValue(19691L));
                    }
                });

                ITimedLock2 tl = new TimedLock2();
                tl.acquireLock(m_cb1);
                assertEquals(false, tl.isAvailable());
                tl.acquireLock(m_cb1);
                assertFalse(tl.isAvailable());
                assertEquals(m_cb1, tl.getCurrentCB());
                assertEquals(19691L, tl.getCurrentTimestamp());
                tl.releaseLock(m_cb1);                
            }
            //double acquire same thread different cb
            {
                ctx.checking(new Expectations() {
                    {
                        exactly(1).of(t).currentTimeMillis();will(returnValue(19691L));
                    }
                });

                ITimedLock2 tl = new TimedLock2();
                tl.acquireLock(m_cb1);
                assertEquals(false, tl.isAvailable());
                tl.acquireLock(m_cb2);
                assertFalse(tl.isAvailable());
                assertEquals(m_cb1, tl.getCurrentCB());
                assertEquals(19691L, tl.getCurrentTimestamp());
                tl.releaseLock(m_cb1);                
            }            
            {
                
            }
            // release with no acquire
            {
                ITimedLock2 tl = new TimedLock2();
                tl.releaseLock(m_cb1);
                assertTrue(tl.isAvailable());
                assertEquals(null, tl.getCurrentCB());
                assertEquals(0L, tl.getCurrentTimestamp());            
            }

            // acquire/release with different cb
            {
                ctx.checking(new Expectations() {
                    {
                        exactly(1).of(t).currentTimeMillis();
                        will(returnValue(1992L));
                    }
                });

                ITimedLock2 tl = new TimedLock2();
                tl.acquireLock(m_cb1);
                assertFalse(tl.isAvailable());
                tl.releaseLock(m_cb2);

                assertFalse(tl.isAvailable());
                assertEquals(m_cb1, tl.getCurrentCB());
                assertEquals(1992L, tl.getCurrentTimestamp());

                tl.releaseLock(m_cb1);
                assertTrue(tl.isAvailable());
                assertEquals(null, tl.getCurrentCB());
                assertEquals(0L, tl.getCurrentTimestamp());
            }
        } finally {
            ApiStack.popInterface(1);
        }
    }
}
