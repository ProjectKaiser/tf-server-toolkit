/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor.BasicPeriodicalTask;

public class PeriodicalTasksExecutorTest extends BasicServerRunningTestCase {
	
	static class TestCommand extends PeriodicalTasksExecutor.BasicPeriodicalTask{
		@Override
		public void run() {
		}
	}

	
	public void test(){
		{
			PeriodicalTasksExecutor te = new PeriodicalTasksExecutor();
			te.init();
			try{
			
				BasicPeriodicalTask command = new TestCommand();
				te.scheduleWithFixedDelay(command, 100, 100, TimeUnit.MILLISECONDS);
				
				ScheduledFuture<?> feat;
				assertNotNull(feat = te.getTaskFeature(TestCommand.class));
				assertFalse(feat.isDone());
				assertFalse(feat.isCancelled());
			}finally{
				te.finit();
			}
		}
		{
					
		}
		
	}
	
	
}
