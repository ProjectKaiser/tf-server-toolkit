/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.services;

import java.util.Map;

import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor.BasicPeriodicalTask;
import com.triniforce.server.plugins.kernel.services.PKEPServices.EServiceNotFound;
import com.triniforce.server.plugins.kernel.tables.TDbQueues;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ICheckInterrupted;
import com.triniforce.utils.IName;

public class ServicesTest extends ServicesTestCase {
	
	public static class SimpleService extends Service{
		
	}
	
	static final long SQS_ID = 909L;
	public static class SimpleQueuedService extends EP_QueuedService{

		public SimpleQueuedService() {
			super(SQS_ID);
		}
		
	}
	
	static final long SRVC_IDS[] = {910L,911L,912L,913L,914L,915L,916L};

	@Override
    protected void setUp() throws Exception {
        m_pool = null;
		addPlugin(new PKPlugin() {
			
			@Override
			public void doRegistration() {
				PKEPServices ss = (PKEPServices) getRootExtensionPoint().getExtensionPoint(PKEPServices.class);

				for(int i =0; i<SRVC_IDS.length; i++){
					final String srvName = "ServicesTest_Service_"+SRVC_IDS[i];
//					ss.registerService(SRVC_IDS[i], srvName);
					EP_QueuedService srv = new EP_QueuedService(SRVC_IDS[i]){
						String m_name = srvName;
						public String getName() {
							return m_name;
						};
					};
					ss.putExtension(srvName, srv);
				}
			}
			
			@Override
			public void doExtensionPointsRegistration() {}
		});
        super.setUp();
        

        ISrvSmartTran tr = ApiStack.getInterface(ISrvSmartTran.class);
        tr.delete(TDbQueues.class, new IName[]{}, new Object[]{});
        ISrvSmartTranFactory.Helper.commitAndStartTran();
    }
	

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    void startStopService(long serviceId) throws EServiceNotFound {
        PKEPServices ss = getServices();
        IService s = ss.getService(serviceId);
        s.start();
        s.stop();
    }

	void printStacks() {
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        for (Thread t : stacks.keySet()) {
            trace("==================== Stack for " + t.getName());
            StackTraceElement[] trace = stacks.get(t);
            String s = "";
            for (StackTraceElement tr : trace) {
                s = s + tr.toString() + "\n";
            }
            trace(s);
        }
    }

    public void stopServicesAndWait() throws EServiceNotFound {
        getServer().stopServices();
        PKEPServices ss = getServices();
        IService s = ss.getService(SM_ID);
        int cnt = 0;
        int cntThreshold = 120;
        while (s.getState() != IService.State.STOPPED) {
            cnt++;
            if (cnt > cntThreshold) {
                printStacks();
                break;
            }
            ISrvSmartTranFactory.Helper.commitAndStartTran();
            ICheckInterrupted.Helper.sleep(1000);
        }
        ICheckInterrupted.Helper.sleep(1000);
        assertEquals(IService.State.STOPPED, s.getState());
    }

    int m_cnt;

    class MyTask extends BasicPeriodicalTask {
        public MyTask() {
            this.delay = 10;
            this.initialDelay = 10;
        }

        public void run() {
            m_cnt++;
        }
    }

    public void testStartStop() throws EServiceNotFound {       
//        ICurrentUser.Helper.pushUser(ReservedKeys.ADMIN.getKey(),
//                ReservedKeys.ADMIN.getKey());

        IBasicServer server = ApiStack.getInterface(IBasicServer.class);
        MyTask mytask = new MyTask();
        server.addPeriodicalTask(mytask);
        try {

            {// test that task is not started
                ICheckInterrupted.Helper.sleep(200);
                assertEquals(0, m_cnt);
            }

            getServer().startServices();
            ISrvSmartTranFactory.Helper.commitAndStartTran();
            try {
                {// wait untill mytask run few times
                    do {
                        ICheckInterrupted.Helper.sleep(100);
                    } while (m_cnt < 11);
                    assertTrue(m_cnt > 10);
                }
            } finally {
                this.stopServicesAndWait();
//                ICurrentUser.Helper.popUser();
            }
            {// test that task does not continue
                m_cnt = 0;
                assertEquals(0, m_cnt);
                ICheckInterrupted.Helper.sleep(200);
                assertEquals(0, m_cnt);
            }            
        } finally {
            server.getPeriodicalTasks().remove(mytask);
        }
    }

    public void waitForState(long serviceId, IService.State state) throws EServiceNotFound {
        ApiAlgs.getLog(this).trace("waiting service: " + serviceId + ", state: "+ state.toString());
        PKEPServices ss = getServices();
        IService s = ss.getService(serviceId);
        while (s.getState() != state) {
            ISrvSmartTranFactory.Helper.commitAndStartTran();
            ICheckInterrupted.Helper.sleep(100);
        }
    }

    public void testStartStopWaiting() throws EServiceNotFound {
//        ICurrentUser.Helper.pushUser(ReservedKeys.ADMIN.getKey(),
//                ReservedKeys.ADMIN.getKey());
        getServer().startServices();
        commitAndStartTran();
        try {
            waitForState(SM_ID,
                    IService.State.RUNNING);

            waitForState(SRVC_IDS[0],
                    IService.State.RUNNING);
            waitForState(SRVC_IDS[1],
                    IService.State.RUNNING);
            waitForState(SRVC_IDS[2],
                    IService.State.RUNNING);
            waitForState(SRVC_IDS[3],
                    IService.State.RUNNING);
            waitForState(SRVC_IDS[4],
                    IService.State.RUNNING);
            waitForState(SRVC_IDS[5],
                    IService.State.RUNNING);
            waitForState(SRVC_IDS[6],
                    IService.State.RUNNING);            

        } finally {
            this.stopServicesAndWait();
        }
    }
    
    static final long NOT_EXISTING_SERVICE = 532056206342L;

    @Override
    public void test() throws Exception {
        IDbQueueFactory.Helper.cleanQueue(SM_ID);
        PKEPServices ss = getServices();
        {
//	        EP_ServiceManager sm = new EP_ServiceManager(SM_ID);
//	        ss.putExtension(sm.getClass().getName(), sm);
        }
        EP_ServiceManager sm = (EP_ServiceManager) ss.getService(SM_ID);
        assertNotNull(sm);
        assertEquals(IService.State.STOPPED, sm.getState());
        assertEquals(SM_ID, sm.getId());

        {// get non-existing service
            try {
                ss.getService(NOT_EXISTING_SERVICE);
                fail();
            } catch (EServiceNotFound e) {
            }
        }
    }

    public void testStartStopWithSubservices() throws EServiceNotFound {
        PKEPServices ss = getServices();
//        ss.registerServiceManager(new EP_ServiceManager(ReservedKeys.SRV_SERVICE_MANAGER.getKey()));
        
        IDbQueue smq = IDbQueueFactory.Helper
                .getQueue(SM_ID);

        ServiceManagerTest.cleanSMQueue();

        {// get service
            IService srvSM = ss.getService(SM_ID);
            assertEquals(IService.State.STOPPED, srvSM.getState());
            srvSM.start();
            assertEquals(IService.State.RUNNING, srvSM.getState());
            // get same service - must be from cache
            assertSame(srvSM, ss.getService(SM_ID));
            srvSM.stop();
        }
//        if (true)
//            return;

        {// start
            assertNull(smq.get(0));
            ss.startStopWithSubservices(SM_ID, true);
            ServiceTest.restartTran(false);// to see changes from parallel
            // transaction
            EP_ServiceManager.StartStopServiceCmd cmd = (EP_ServiceManager.StartStopServiceCmd) smq
                    .get(0);
            assertNotNull(cmd);
            assertEquals(SM_ID, cmd.id);
            assertEquals(true, cmd.isStart());
            ServiceTest.restartTran(true);
        }
        {// stop
            assertNull(smq.get(0));
            ss.startStopWithSubservices(SM_ID, false);
            ServiceTest.restartTran(false);// to see changes from parallel
            // transaction
            EP_ServiceManager.StartStopServiceCmd cmd = (EP_ServiceManager.StartStopServiceCmd) smq
                    .get(0);
            assertNotNull(cmd);
            assertEquals(SM_ID, cmd.id);
            assertEquals(false, cmd.isStart());
            ServiceTest.restartTran(true);
        }

    }

}
