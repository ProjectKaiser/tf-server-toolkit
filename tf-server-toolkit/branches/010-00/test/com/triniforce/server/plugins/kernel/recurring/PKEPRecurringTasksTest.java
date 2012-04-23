/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.recurring;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.TFUtils;

public class PKEPRecurringTasksTest extends BasicServerTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		getServer().enterMode(Mode.Running);
	}
	
    @Override
    protected void tearDown() throws Exception {
        PKEPRecurringTasks rts = new PKEPRecurringTasks();
        rts.deleteAllTasks();
        ISrvSmartTranFactory.Helper.commitAndStartTran();
        getServer().leaveMode();
        super.tearDown();
    }
    
    static int prevCallNumber;
    
    static class SimpleTask implements IPKEPRecurringTask{

        public long m_id;
        public long m_start;
        public long m_currentTime;
        public boolean m_isTooLate;
        int callNumber;

        //@Override
        public void processTask(long id, long start, long currentTime,
                boolean isTooLate) {
                    m_id = id;
                    m_start = start;
                    m_currentTime = currentTime;
                    m_isTooLate = isTooLate;
                    callNumber = ++prevCallNumber;
        }
    }
    
    static class SimpleTask2 extends SimpleTask{

    }
    
    static class SimpleTaskReinsert extends SimpleTask{

        public PKEPRecurringTasks m_rts;
        
        @Override
        public void processTask(long id, long start, long currentTime,boolean isTooLate) {
            m_rts.createOrUpdateTask(id, this.getClass(), currentTime + 31415926);
        }        
    }
    
    
    static class SimpleTaskException extends SimpleTask{
                
        @Override
        public void processTask(long arg0, long arg1, long arg2, boolean arg3) {
            super.processTask(arg0, arg1, arg2, arg3);
            TFUtils.assertTrue(false, Long.toString(arg0));
        }
    }    
    
    DateTimeZone tzMsc = DateTimeZone.forID("Europe/Moscow");
    DateTime april2012Msc = new DateTime(2012, 04, 1, 12, 0, tzMsc);
    //2011-03-27 01:59:59.999+03:00
    DateTime prevTransitionMsc = new DateTime(tzMsc.previousTransition(april2012Msc.getMillis()), tzMsc);

    
    @Override
    public void test() throws Exception {
        
        IIdGenerator idGen = ApiStack.getInterface(IIdGenerator.class);

        long id1 = idGen.getKey();
        long id2 = idGen.getKey();
        SimpleTask st1 = new SimpleTask();
        SimpleTask2 st2 = new SimpleTask2();
        SimpleTaskException ste = new SimpleTaskException();
        
        
        //nothing in queue
        {
            PKEPRecurringTasks rts = new PKEPRecurringTasks();
            rts.deleteAllTasks();
            
            rts.putExtension(st1);
        
            rts.processTasksInTransactions(1L);
            assertEquals(0, st1.callNumber);
        }
        
        //test reinsert
        {
            PKEPRecurringTasks rts = new PKEPRecurringTasks();
            rts.deleteAllTasks();
            long start1 = prevTransitionMsc.getMillis();
            long current = start1 + 10;
            
            SimpleTaskReinsert str = new SimpleTaskReinsert();
            str.m_rts = rts;
            rts.putExtension(str);
            
            rts.createOrUpdateTask(id1, str.getClass(), start1);
            rts.processTasksInTransactions(current);
            TRecurringTasks.Data data = rts.peekNextData();
            assertEquals(id1, data.id);
            assertEquals(current + 31415926, data.start);
            assertEquals(current + 31415926, rts.getNextTime());
        }
        
        //single not late instance
        {
            PKEPRecurringTasks rts = new PKEPRecurringTasks();
            rts.deleteAllTasks();
            
            long start1 = prevTransitionMsc.getMillis();
            long start2 = start1 - 1000;
            long current = start1 + 10;
            
            
            rts.putExtension(st1);
            rts.putExtension(st2);
            
            rts.createOrUpdateTask(id1, st1.getClass(), start1);
            rts.createOrUpdateTask(id2, st2.getClass(), start2);
            TRecurringTasks.Data data = rts.peekNextData(); 
            assertNotNull(rts.peekNextData());
            assertEquals(start2, data.start);
            
            int prevnCalls = prevCallNumber;
            rts.processTasksInTransactions(current);
            //st2 called first
            assertEquals(prevnCalls + 1, st2.callNumber);
            assertEquals(id2, st2.m_id);
            assertEquals(current, st2.m_currentTime);
            assertEquals(false, st2.m_isTooLate);
            assertEquals(prevnCalls + 2, st1.callNumber);
            assertEquals(id1, st1.m_id);
            assertEquals(current, st1.m_currentTime);
            assertEquals(false, st1.m_isTooLate);
        }
        
        //task1 is late
        {
            PKEPRecurringTasks rts = new PKEPRecurringTasks();
            rts.deleteAllTasks();            
            
            long start1 = prevTransitionMsc.getMillis() - 10;
            long start2 = start1 - 1000 -  PKEPRecurringTasks.DEFAULT_PAST_THRESHOLD;
            long current = start1 + 10;
//            RTPeriod period = null;
            
            rts.putExtension(st1);
            rts.putExtension(st2);
            
            rts.createOrUpdateTask(id1, st1.getClass(), start1);
            rts.createOrUpdateTask(id2, st2.getClass(), start2);

            int prevnCalls = prevCallNumber;
            rts.processTasksInTransactions(current);
            //st2 called first
            assertEquals(prevnCalls + 1, st2.callNumber);
            assertEquals(id2, st2.m_id);
            assertEquals(current, st2.m_currentTime);
            assertEquals(true, st2.m_isTooLate);
            assertEquals(prevnCalls + 2, st1.callNumber);
            assertEquals(id1, st1.m_id);
            assertEquals(current, st1.m_currentTime);
            assertEquals(false, st1.m_isTooLate);
        }
        
        //test exception handling
        {
            incExpectedLogErrorCount(1);

            long start1 = prevTransitionMsc.getMillis() - 10;
            long start2 = start1 - 1000;
            long current = start1 + 10;
            
            PKEPRecurringTasks rts = new PKEPRecurringTasks();
            rts.deleteAllTasks();                       
            rts.putExtension(st1);
            rts.putExtension(ste);
            
            rts.createOrUpdateTask(id1, st1.getClass(), start1);
            rts.createOrUpdateTask(id2, ste.getClass(), start2);
            
            assertEquals(0, rts.getExceptionCount());
            int prevnCalls = prevCallNumber;
            
            ISrvSmartTranFactory.Helper.commitAndStartTran();
            
            rts.processTasksInTransactions(current);
            assertEquals(1, rts.getExceptionCount());
            //st2 called first
            assertEquals(prevnCalls + 1, ste.callNumber);
            assertEquals(id2, ste.m_id);
            assertEquals(current, ste.m_currentTime);
            assertEquals(false, ste.m_isTooLate);
            assertEquals(prevnCalls + 2, st1.callNumber);
            assertEquals(id1, st1.m_id);
            assertEquals(current, st1.m_currentTime);
            assertEquals(false, st1.m_isTooLate);
        }
        
        //test deleteTask
        {
            long start1 = prevTransitionMsc.getMillis() - 10;
            long start2 = start1 - 1000;
            //long current = start1 + 10;
            
            PKEPRecurringTasks rts = new PKEPRecurringTasks();
            rts.deleteAllTasks();                       
            rts.putExtension(st1);
            rts.putExtension(st2);
            
            rts.createOrUpdateTask(id1, st1.getClass(), start1);
            rts.createOrUpdateTask(id2, st2.getClass(), start2);
            TRecurringTasks.Data data = rts.peekNextData(); 
            assertEquals(start2, data.start);
            rts.deleteTask(id2);
            data = rts.peekNextData(); 
            assertEquals(start1, data.start);            
        }
        
        //test nextTime
        {
            long start1 = prevTransitionMsc.getMillis() - 10;
            long start2 = start1 - 1000;
            long current = start1 - 10;
            
            PKEPRecurringTasks rts = new PKEPRecurringTasks();
            rts.deleteAllTasks();                       
            rts.putExtension(st1);
            rts.putExtension(st2);
            
            assertEquals(0, rts.getNextTime());
            
            rts.createOrUpdateTask(id1, st1.getClass(), start1);
            rts.createOrUpdateTask(id2, st2.getClass(), start2);
            
            //st2
            assertEquals(1, rts.processTasksInTransactions(current));
            
            assertNotNull(rts.peekNextData());
            assertEquals(start1, rts.getNextTime());
            
            assertEquals(0, rts.processTasksInTransactions(current));
            
            //st1
            assertEquals(1, rts.processTasksInTransactions(start1));
            assertNull(rts.peekNextData());
            assertEquals(start1 + PKEPRecurringTasks.ALWAYS_READ_INTERVAL, rts.getNextTime());
            
            //create st2 again,  start2 < start1, process at start2
            int prevNumber = prevCallNumber;
            assertTrue(start2 < start1);
            rts.createOrUpdateTask(id2, st2.getClass(), start2);
            assertEquals(0, rts.getNextTime());
            assertEquals(1, rts.processTasksInTransactions(start2));
            assertEquals(prevNumber +1, st2.callNumber);
            assertEquals(start2 + PKEPRecurringTasks.ALWAYS_READ_INTERVAL, rts.getNextTime());            
            
        }
        
        
        
    }

}
