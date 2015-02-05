/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIPeriodicalTask;
import com.triniforce.utils.ApiStack;

public class QSyncManagerTask extends PKEPAPIPeriodicalTask {

	public Class getImplementedInterface() {
		return QSyncManagerTask.class;
	}

	@Override
	public void run() {
		ApiStack.getInterface(IQSyncManager.class).onEveryMinute();
	}

}
