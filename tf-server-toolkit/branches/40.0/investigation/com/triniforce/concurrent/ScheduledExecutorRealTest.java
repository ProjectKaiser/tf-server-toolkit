/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.ICheckInterrupted;

public class ScheduledExecutorRealTest extends TFTestCase {
	
	final int N_THREADS = 1000;
	
	class MyR implements Runnable{
		
		private final int m_n;
		private int m_cnt = 0;
        private final boolean m_raise;
        private final CountDownLatch m_cl;
		

        public MyR(CountDownLatch cl, int n, boolean raise) {
            m_cl = cl;
            m_n = n;
            m_raise = raise;
        }

		@Override
		public void run() {
			//trace("Run :" + m_n);
			m_cnt++;
			if(m_cnt == 2){
				m_cl.countDown();
				trace("Finished :" + m_n);
			}
			if(m_raise){
			    ICheckInterrupted.Helper.sleep(1000);
			    throw new NullPointerException();
			}
			
		}
	}
	
	public void testManyTasks() throws Exception {
	    
	    CountDownLatch cl = new CountDownLatch(N_THREADS/2);
		ScheduledExecutor se = new ScheduledExecutor(2, 4);
		for(int i = 0; i< N_THREADS; i++){
			Runnable r = new MyR(cl, i, false);
			se.scheduleWithFixedDelay(r, 100, 100, TimeUnit.MILLISECONDS);
		}


		while(!cl.await(1000, TimeUnit.MILLISECONDS)){
		    trace(cl.getCount());
		}
		se.shutdownNow();
		assertTrue(se.awaitTermination(1000, TimeUnit.MILLISECONDS));
		
	}
	
    public void testTasksWithExceptions() throws Exception {
        
        /*
         * 
         * tasks 1 and 2 sleeps, raise exceptions at the end of the sleep
         * task3 gets RejectedExecutionException (both threads busy)  and must be rescheduled
         * 
         * 
         */
        final CountDownLatch cl = new CountDownLatch(3);
        ScheduledExecutor se = new ScheduledExecutor(2, 2);
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                trace("r1 enters");
                ICheckInterrupted.Helper.sleep(1000);
                trace("r1 leaves");
                cl.countDown();
                throw new NullPointerException();
            }
        };
        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                trace("r2 enters");
                ICheckInterrupted.Helper.sleep(1000);
                trace("r2 leaves");
                cl.countDown();
                throw new NullPointerException();
            }
        };
        Runnable r3 = new Runnable() {
            @Override
            public void run() {
                trace("r3 enters");
                cl.countDown();
            }
        };

        se.scheduleWithFixedDelay(r1, 100, 101, TimeUnit.MILLISECONDS);
        se.scheduleWithFixedDelay(r2, 100, 102, TimeUnit.MILLISECONDS);
        se.scheduleWithFixedDelay(r3, 200, 103, TimeUnit.MILLISECONDS);

        while(!cl.await(1000, TimeUnit.MILLISECONDS)){
            trace("cl=" + cl.getCount());
        }
        se.shutdownNow();
        assertTrue(se.awaitTermination(1000, TimeUnit.MILLISECONDS));
        
    }	
	
	@Override
	public void test() throws Exception {
		Runnable r1 = new Runnable() {
			
			@Override
			public void run() {
				trace("Run 1");
			}
		};
		Runnable r2 = new Runnable() {
			@Override
			public void run() {
				trace("Run 2");
				ICheckInterrupted.Helper.sleep(1000);
			}
		};
		Runnable rSleep = new Runnable() {
			@Override
			public void run() {
				trace("Run and sleep");
				ICheckInterrupted.Helper.sleep(100 * 1000);
			}
		};
		
		Runnable rOnce = new Runnable() {
			@Override
			public void run() {
				trace("Run once");
			}
		};
		Runnable rOnce2 = new Runnable() {
			@Override
			public void run() {
				trace("Run once2");
			}
		};
		
		ScheduledExecutor se = new ScheduledExecutor(2, 4);
		se.scheduleWithFixedDelay(rOnce, 100, -1002, TimeUnit.MILLISECONDS);
		
		se.scheduleWithFixedDelay(rOnce2, 1200, -1002, TimeUnit.MILLISECONDS);
		
		se.scheduleWithFixedDelay(rSleep, 1000, 1003, TimeUnit.MILLISECONDS);
		
		se.scheduleWithFixedDelay(r2, 1100, 1002, TimeUnit.MILLISECONDS);

		se.scheduleWithFixedDelay(r1, 1050, 1001, TimeUnit.MILLISECONDS);

		Thread.sleep(5 * 1000);
		se.shutdownNow();
		assertTrue(se.awaitTermination(1000, TimeUnit.MILLISECONDS));
	}

}
