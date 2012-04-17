/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.recurring;

import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor.BasicPeriodicalTask;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class PTRecurringTasks extends BasicPeriodicalTask{

	@Override
	public void run() {
		IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
		PKEPRecurringTasks rts = (PKEPRecurringTasks) bs.getExtensionPoint(PKEPRecurringTasks.class);
		rts.processTasksInTransactions(ITime.ITimeHelper.currentTimeMillis());
	}
	
}
