/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.qsync.impl;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.dbo.PKEPDBObjects;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.qsync.intf.IQSyncer;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiAlgs;

public class QSyncPlugin extends PKPlugin {
	
	public static Object syncObj = new Object();
	
	public static class TestSyncer extends DboQsyncQueue implements IQSyncer{

		boolean m_initiallySynced = false;
		List<Object> m_synced = new ArrayList<Object>();

		public TestSyncer() {
			super(40001L, TestSyncer.class);
		}

		@Override
		public IQSyncer createSyncer() {
			return this;
		}

		public void connectToQueue(long qid) {}

		public void initialSync() {
			ApiAlgs.assertTrue(!m_initiallySynced,"");
			m_initiallySynced  = true;
			synchronized (syncObj) {
				syncObj.notify();
			}
		}

		public void finit(Throwable t) {}

		public List synced() {
			return m_synced;
		}

		public void sync(Object o) {
			m_synced.add(o);
			
			synchronized (syncObj) {
				syncObj.notify();
			}

		} 
		
	}
	
	@Override
	public void doRegistration() {
		putExtension(PKEPDBObjects.class, TestSyncer.class);
	}
	
	@Override
	public void doExtensionPointsRegistration() {
	}

	public TestSyncer getSyncer(IBasicServer s) {
		return (TestSyncer) s.getExtension(PKEPDBObjects.class.getName(), TestSyncer.class.getName()).getInstance();
	}
}
