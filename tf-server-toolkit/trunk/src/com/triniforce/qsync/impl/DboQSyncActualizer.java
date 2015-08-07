/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.util.List;

import com.triniforce.dbo.DBOActualizer;
import com.triniforce.dbo.IDBObject;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.utils.ApiStack;

public class DboQSyncActualizer extends DBOActualizer{

	public DboQSyncActualizer() {
		super(false, Mode.Running);
	}
	
	@Override
	public void actualize(List<IDBObject> dboList) {
		IQSyncManager qsMan = ApiStack.getInterface(IQSyncManager.class);
		for (IDBObject idbObject : dboList) {
			DboQsyncQueue qs = (DboQsyncQueue)idbObject;
			long syncerId = ApiStack.getInterface(INamedDbId.class).createId(qs.getClass().getName());
			Long qId = qs.getQueueId();
			if(qId == null){
				qs.setQueueId(syncerId);
				qId = syncerId;
			}
			qsMan.registerQueue(qId, syncerId);
		}
	}

}
