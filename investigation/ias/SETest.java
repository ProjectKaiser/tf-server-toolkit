/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.triniforce.db.test.TFTestCase;

@Deprecated
public class SETest extends TFTestCase {

	
	static class Task1 implements Runnable{
		private long slp;
		public Task1(long slp) {
			this.slp = slp;
		}
		@Override
		public void run() {
			try {
				Thread.sleep(slp);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	static class Task2 implements Runnable{
		private long slp;
		public Task2(long slp) {
			this.slp = slp;
		}
		@Override
		public void run() {
			try {
				Thread.sleep(slp);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	static class Task3 extends Task2{
		public Task3(long slp) {
			super(slp);
		}
	}
	static class Task4 extends Task2{
		public Task4(long slp) {
			super(slp);
		}
	}
	
	
	@Override
	public void test() throws Exception {
		ScheduledExecutorService se = Executors.newScheduledThreadPool(8, new ThreadFactory(){
        	int num = 0;

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "scheduler" + num++);
			}
        	
        });
		List<ScheduledFuture> feats = new ArrayList<ScheduledFuture>();
		
		for(int i=0; i<4; i++)
			feats.add(se.scheduleWithFixedDelay(new Task1(20L), 100, 100, TimeUnit.MILLISECONDS));
		for(int i=0; i<4; i++)
			feats.add(se.scheduleWithFixedDelay(new Task2(40L), 200, 200, TimeUnit.MILLISECONDS));
		for(int i=0; i<4; i++)
			feats.add(se.scheduleWithFixedDelay(new Task3(80L), 300, 300, TimeUnit.MILLISECONDS));
		for(int i=0; i<4; i++)
			feats.add(se.scheduleWithFixedDelay(new Task4(60L), 2000, 2000, TimeUnit.MILLISECONDS));

		for(int i = 0 ; i< 500; i++){
			int canceled = 0, done=0;
			
			for (ScheduledFuture scheduledFuture : feats) {
				if(scheduledFuture.isCancelled())
					canceled ++;
				if(scheduledFuture.isDone())
					done ++;
				trace("delay: " + scheduledFuture.getDelay(TimeUnit.MILLISECONDS));
				
			}
			trace("Cancelled: " + canceled + ", done: " + done);
			Thread.sleep(1000);
		}
		
		
		se.shutdownNow();
		assertTrue(se.awaitTermination(1000, TimeUnit.MILLISECONDS));

	}
}
