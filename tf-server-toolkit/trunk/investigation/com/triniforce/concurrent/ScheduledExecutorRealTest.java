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
	
	CountDownLatch m_l = new CountDownLatch(N_THREADS);
	
	class MyR implements Runnable{
		
		private final int m_n;
		private int m_cnt = 0;
		

		public MyR(int n) {
			m_n = n;
		}

		@Override
		public void run() {
			//trace("Run :" + m_n);
			m_cnt++;
			if(m_cnt == 2){
				m_l.countDown();
				trace("Finished :" + m_n);
			}
		}
	}
	
	public void testManyTasks() throws Exception {
		ScheduledExecutor se = new ScheduledExecutor(2, 4);
		for(int i = 0; i< N_THREADS; i++){
			Runnable r = new MyR(i);
			se.scheduleWithFixedDelay(r, 100, 100, TimeUnit.MILLISECONDS);
		}

		m_l.await();
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
