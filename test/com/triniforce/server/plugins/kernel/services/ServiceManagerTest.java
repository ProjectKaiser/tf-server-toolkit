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
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ICheckInterrupted;

public class ServiceManagerTest extends BasicServerTestCase {
	
	static final long SM_ID = 900L;
	
	public static class SimpleService extends EPService{
		
	}
	
	static final long SQS_ID = 909L;
	public static class SimpleQueuedService extends EP_QueuedService{

		public SimpleQueuedService() {
			super(SQS_ID);
		}
		
	}
	
	@Override
    protected void setUp() throws Exception {
		addPlugin(new PKPlugin() {
			
			@Override
			public void doRegistration() {
				PKEPServices ss = (PKEPServices) getRootExtensionPoint().getExtensionPoint(PKEPServices.class);
				ss.registerServiceManager(new EP_ServiceManager(SM_ID));
				ss.registerService(SQS_ID, SimpleQueuedService.class.getName());
				ss.putExtension(SimpleQueuedService.class);
				ss.putExtension(SimpleService.class);
				ss.putExtension(ThrdWatcher.class);
			}
			
			@Override
			public void doExtensionPointsRegistration() {
				// TODO Auto-generated method stub
				
			}
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

    @Override
    public void test() throws Exception {
        ApiAlgs.getLog(this).trace("Hi");        

        cleanSMQueue();

//        Long idAR = getIdByKey(AutoRegistration.class.getName());
        String arName = SimpleService.class.getName();
        Long idMailer = getIdByKey(SimpleQueuedService.class.getName());

        PKEPServices ss = getServices();

        EP_IService srvAR = ss.getService(arName);
        assertEquals(EP_IService.State.STOPPED, srvAR.getState());
        EP_IService srvMailer = ss.getService(idMailer);
        assertEquals(EP_IService.State.STOPPED, srvMailer.getState());        

        EP_IService srvSM = ss.getService(SM_ID);
        assertEquals(EP_IService.State.STOPPED, srvSM.getState());

        srvSM.start();
        assertEquals(EP_IService.State.RUNNING, srvSM.getState());
        assertEquals(EP_IService.State.STOPPED, srvAR.getState());

        commitAndStartTran();
        ss.startStopWithSubservices(SM_ID, true);
        int i = 0;
        while (srvAR.getState() != EP_IService.State.RUNNING) {
            ICheckInterrupted.Helper.sleep(100);
            trace("wait1.." + (i++));
        }
        while (srvMailer.getState() != EP_IService.State.RUNNING) {
            ICheckInterrupted.Helper.sleep(100);
            trace("wait1a.." + (i++));
        }        
        ss.startStopWithSubservices(SM_ID,
                false);
        while (srvAR.getState() != EP_IService.State.STOPPED) {
            ICheckInterrupted.Helper.sleep(100);
            trace("wait2.." + (i++));
        }
        while (srvMailer.getState() != EP_IService.State.STOPPED) {
            ICheckInterrupted.Helper.sleep(100);
            trace("wait2a.." + (i++));
        }        
        while (srvSM.getState() != EP_IService.State.STOPPED) {
            ICheckInterrupted.Helper.sleep(100);
            trace("wait3.." + (i++));
        }

    }

	private PKEPServices getServices() {
		return (PKEPServices) ApiStack.getInterface(IBasicServer.class).getExtensionPoint(PKEPServices.class);
	}
	
    protected void commitAndStartTran() {
        ISrvSmartTranFactory.Helper.commit();
        ISrvSmartTranFactory.Helper.pop();
        ISrvSmartTranFactory.Helper.push();
    }    
    
}
