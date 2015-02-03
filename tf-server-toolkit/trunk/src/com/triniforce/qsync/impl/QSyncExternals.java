/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.lang.reflect.Method;

import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.qsync.intf.IQSyncManagerExternals;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.server.srvapi.BasicServerTask;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.utils.ApiStack;

public class QSyncExternals implements IQSyncManagerExternals{
	
	static class EQSyncerNotFound extends RuntimeException{
		private static final long serialVersionUID = 1L;
	
		public EQSyncerNotFound(String msg) {
			super(msg);
		}
	}

	public IQSyncer getQSyncer(long qid, Long syncerId) {
		IQSyncer res;
		String qSyncerName = ApiStack.getInterface(INamedDbId.class).getName(syncerId);
		
		IPKExtensionPoint ep = ApiStack.getInterface(IBasicServer.class).getExtensionPoint(PKEPDBObjects.class);
		if(ep.getExtensions().containsKey(qSyncerName)){
			// static syncer registration
			DboQsyncQueue dbo = ep.getExtension(qSyncerName).getInstance();
			res = dbo.createSyncer();
		}
		else{
			//dynamic
			try {
				Class<?> cls = Class.forName(qSyncerName);
				Method m = cls.getMethod("newInstance", new Class[]{});
				res = (IQSyncer) m.invoke(null, new Object[]{});
			} catch (Exception e) {
				throw new EQSyncerNotFound(qSyncerName);
			}
		}
		return res;
	}

	public void runSync(Runnable r) {
		ITaskExecutors te = ApiStack.getInterface(ITaskExecutors.class);
		te.execute(ITaskExecutors.shortTaskExecutorKey, new Task(r));
	}
	
	static class Task extends BasicServerTask{
		
		private Runnable m_runnable;

		public Task(Runnable r) {
			m_runnable = r;
		}

		public void run() {
			m_runnable.run();
			
		}
		
	}


}
