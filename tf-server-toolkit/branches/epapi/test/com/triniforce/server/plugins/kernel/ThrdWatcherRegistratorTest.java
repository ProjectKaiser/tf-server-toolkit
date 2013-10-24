/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.test.BasicServerApiEmu;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator.ThreadInfo;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class ThrdWatcherRegistratorTest  extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        BasicServerApiEmu ae = new BasicServerApiEmu();
        ae.setCurrentTimeMillis(0L);        
        
        ApiStack.pushInterface(ITime.class, ae);
        try{
        
            Thread t1 = new Thread();
            t1.setName("t1");
            Thread t2 = new Thread();
            t2.setName("t2");
            Thread t3 = new Thread();
            t3.setName("t3");
            
            ThrdWatcherRegistrator tw = new ThrdWatcherRegistrator();
            tw.setITime(ApiStack.getInterface(ITime.class));
            IThrdWatcherRegistrator itw = tw;
            assertEquals(0, itw.getWaitingThreads(0L).size());
            
            ae.setCurrentTimeMillis(1L);
            itw.registerThread(t1, "t1 info");
            ae.setCurrentTimeMillis(2L);
            itw.registerThread(t2, "t2 info");
            ae.setCurrentTimeMillis(3L);
            itw.registerThread(t3, "t3 info");
            ae.setCurrentTimeMillis(4);
            
            assertEquals(3, itw.getWaitingThreads(0L).size());
            assertEquals(1, itw.getWaitingThreads(2).size());
            assertTrue( itw.getWaitingThreads(2).containsKey(t1));
            assertEquals(2, itw.getWaitingThreads(1).size());
            assertTrue( itw.getWaitingThreads(1).containsKey(t1));
            assertTrue( itw.getWaitingThreads(1).containsKey(t2));
            
            assertTrue(itw.isAnyShortThreadWaiting(0));
            assertTrue(itw.isAnyShortThreadWaiting(2));
            assertTrue(itw.isAnyShortThreadWaiting(1));
            
            itw.registerLongTermOp(t1);
            
            assertFalse(itw.isAnyShortThreadWaiting(2));
            assertTrue(itw.isAnyShortThreadWaiting(1));
            assertTrue( itw.getWaitingThreads(1).containsKey(t1));
            assertTrue( itw.getWaitingThreads(1).containsKey(t2));            
            
            itw.unregisterLongTermOp(t1);
            
            assertFalse(itw.isAnyShortThreadWaiting(2));
            assertTrue(itw.isAnyShortThreadWaiting(1));
            assertFalse( itw.getWaitingThreads(1).containsKey(t1));
            assertTrue( itw.getWaitingThreads(1).containsKey(t2));
            
            itw.unregisterLongTermOp(t2);
            assertFalse(itw.isAnyShortThreadWaiting(1));
            assertFalse( itw.getWaitingThreads(1).containsKey(t1));
            assertFalse( itw.getWaitingThreads(1).containsKey(t2)); 

            ae.setCurrentTimeMillis(5);
            
            assertTrue( itw.getWaitingThreads(0).containsKey(t3));
            assertTrue( itw.getWaitingThreads(0).containsKey(t2));
            assertTrue( itw.getWaitingThreads(0).containsKey(t1));
            assertTrue(itw.isAnyShortThreadWaiting(0));
            itw.unregisterThread(t3);
            
            assertFalse( itw.getWaitingThreads(0).containsKey(t3));
            
            itw.unregisterThread(t2);
            itw.unregisterThread(t1);
            
            assertFalse( itw.getWaitingThreads(0).containsKey(t2));
            assertFalse( itw.getWaitingThreads(0).containsKey(t1));
            
            Thread t4 = new Thread();
            itw.registerThread(t4, null);
            ThreadInfo ti4 = itw.queryThreadInfo(t4);
            assertEquals(1, ti4.getLockCnt());
            itw.registerThread(t4, null);
            assertEquals(2, ti4.getLockCnt());
            itw.unregisterThread(t4);
            assertEquals(1, ti4.getLockCnt());
            itw.unregisterThread(t4);
            assertEquals(0, ti4.getLockCnt());
            assertNull(itw.queryThreadInfo(t4));
            
            itw.unregisterThread(t4);
            
        }finally{
            ApiStack.popInterface(1);
        }
        
    }
    

}
