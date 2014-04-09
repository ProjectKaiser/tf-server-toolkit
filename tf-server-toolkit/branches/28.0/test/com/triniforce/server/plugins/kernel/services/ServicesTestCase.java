/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.services;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.utils.ApiStack;

//TODO: ias: не очень понятно, что это такое
public class ServicesTestCase extends BasicServerTestCase {
	
	static final long SM_ID = 900L;
		
	@Override
    protected void setUp() throws Exception {
		addPlugin(new PKPlugin() {
			
			@Override
			public void doRegistration() {
				PKEPServices ss = (PKEPServices) getRootExtensionPoint().getExtensionPoint(PKEPServices.class);
//				ss.registerServiceManager();
				EP_ServiceManager sm = new EP_ServiceManager(SM_ID);
				ss.putExtension(sm.getName(), sm);
				ss.putExtension(ThrdWatcher.class);
			}
			
			@Override
			public void doExtensionPointsRegistration() {}
		});
        super.setUp();
        getServer().enterMode(Mode.Running);
        
        IThrdWatcherRegistrator twr = ApiStack.getInterface(IThrdWatcherRegistrator.class);
        twr.registerThread(Thread.currentThread(), null);
    }

    @Override
    protected void tearDown() throws Exception {
    	getServer().leaveMode();
        super.tearDown();
    }

    public static Long getIdByKey(String key) {
        IBasicServer rep = ApiStack.getInterface(IBasicServer.class);
        PKEPServices ep = (PKEPServices) rep.getExtensionPoint(PKEPServices.class);
        IPKExtension ext = ep.getExtension(key);
        EP_QueuedService svc = ext.getInstance();
        return svc.getId();
    }

    public static void cleanSMQueue() {
        ISrvSmartTranFactory.Helper.push();
        IDbQueue smq = IDbQueueFactory.Helper
                .getQueue(SM_ID);

        {// clean SM queue
            while (smq.get(0) != null)
                ;
        }
        ISrvSmartTranFactory.Helper.commit();
        ISrvSmartTranFactory.Helper.pop();
    }

	protected PKEPServices getServices() {
		return (PKEPServices) ApiStack.getInterface(IBasicServer.class).getExtensionPoint(PKEPServices.class);
	}
	
    protected void commitAndStartTran() {
        ISrvSmartTranFactory.Helper.commit();
        ISrvSmartTranFactory.Helper.pop();
        ISrvSmartTranFactory.Helper.push();
    }    
    
}
