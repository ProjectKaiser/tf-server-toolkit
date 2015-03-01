/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.qsync.intf.IQSyncManagerExternals;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.server.srvapi.BasicServerTask;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class QSyncExternals implements IQSyncManagerExternals{
	
	private static final int STORED_FUTURES = 10;
	private LinkedList<Future> m_lastFutures;

	public QSyncExternals() {
		m_lastFutures = new LinkedList<Future>();
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
				ApiAlgs.getLog(this).trace("instantiation problem", e);
				throw new EQSyncerNotFound(qSyncerName);
			}
		}
		return res;
	}

	synchronized  public void runSync(Runnable r) {
		ITaskExecutors te = ApiStack.getInterface(ITaskExecutors.class);
		Future future = te.execute(ITaskExecutors.normalTaskExecutorKey, new Task(r));
		m_lastFutures.push(future);
		if(m_lastFutures.size() > STORED_FUTURES)
			m_lastFutures.pop();
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
	
	synchronized public void waitForTaskCompletition() throws InterruptedException, ExecutionException{
		for (Future  f : m_lastFutures) {
			f.get();
		}
	}

	synchronized public void interruptAll() {
		for (Future  f : m_lastFutures) {
			f.cancel(true);
		}
		
	}


}
