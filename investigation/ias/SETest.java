/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.util.concurrent.TimeUnit;

import com.triniforce.concurrent.ScheduledExecutor;
import com.triniforce.db.test.TFTestCase;

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
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
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
		ScheduledExecutor se = new ScheduledExecutor(8, 8);
		for(int i=0; i<4; i++)
			se.scheduleWithFixedDelay(new Task1(20L), 100, 100, TimeUnit.MILLISECONDS);
		for(int i=0; i<4; i++)
			se.scheduleWithFixedDelay(new Task2(40L), 200, 200, TimeUnit.MILLISECONDS);
		for(int i=0; i<4; i++)
			se.scheduleWithFixedDelay(new Task3(80L), 300, 300, TimeUnit.MILLISECONDS);
		for(int i=0; i<4; i++)
			se.scheduleWithFixedDelay(new Task4(60L), 2000, 2000, TimeUnit.MILLISECONDS);

		Thread.sleep(5 * 1000);
		se.shutdownNow();
		assertTrue(se.awaitTermination(1000, TimeUnit.MILLISECONDS));
		
		trace("Max delay : " + se.getMaxDelay());

	}
}
