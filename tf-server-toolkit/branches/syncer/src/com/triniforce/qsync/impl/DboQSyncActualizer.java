/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.triniforce.db.dml.ResSet;
import com.triniforce.dbo.DBOActualizer;
import com.triniforce.dbo.IDBObject;
import com.triniforce.qsync.impl.TQSyncQueues.BL;
import com.triniforce.qsync.intf.IQSyncManager;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiStack;

public class DboQSyncActualizer extends DBOActualizer{

	public DboQSyncActualizer() {
		super(false, Mode.Running);
	}
	
	@Override
	public void actualize(List<IDBObject> dboList) {
		Collection<Long> registered = registeredQueues();
		IQSyncManager qsMan = ApiStack.getInterface(IQSyncManager.class);
		for (IDBObject idbObject : dboList) {
			DboQsyncQueue qs = (DboQsyncQueue)idbObject;
			if(!registered.contains(qs.getQueueId())){
				long syncerId = ApiStack.getInterface(INamedDbId.class).createId(qs.getClass().getName());
				qsMan.registerQueue(qs.getQueueId(), syncerId);
			}
		}
	}
	
	Collection<Long> registeredQueues(){
		BL bl = SrvApiAlgs2.getIServerTran().instantiateBL(TQSyncQueues.BL.class);
		ResSet rs = bl.getIds();
		HashSet<Long> res = new HashSet<Long>();
		while(rs.next())
			res.add(rs.getLong(1));
		return res;
	}

}
