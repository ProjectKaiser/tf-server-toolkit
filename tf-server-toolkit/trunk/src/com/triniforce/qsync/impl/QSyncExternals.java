/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.qsync.intf.IQSyncManagerExternals;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.utils.ApiStack;

public class QSyncExternals implements IQSyncManagerExternals{

	public IQSyncer getQSyncer(long qid, Long syncerId) {
		String dboQSyncerName = ApiStack.getInterface(INamedDbId.class).getName(syncerId);
		
		DboQsyncQueue dbo = ApiStack.getInterface(IBasicServer.class)
		.getExtension(PKEPDBObjects.class.getName(), dboQSyncerName).getInstance();
		
		return dbo.createSyncer();
	}

	public void runSync(Runnable r) {
		r.run();
		
	}

	public void runInitialSync(Runnable r) {
		r.run();
		
	}

}
