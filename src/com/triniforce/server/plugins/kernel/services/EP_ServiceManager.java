/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.triniforce.server.plugins.kernel.services.PKEPServices.EServiceNotFound;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class EP_ServiceManager extends EP_QueuedService {
    public static class StartStopServiceCmd implements Serializable {
        private static final long serialVersionUID = 1L;

        public long id;

        boolean start;// false means stop

		public boolean isStart() {
			return start;
		}
    }

    boolean m_interrupted = false;

    public EP_ServiceManager(long queueId) {
        super(queueId);
    }

    public static void doStartStopSubservices(String svc, boolean bStart) {
        List<String> subServices = getSubservices(svc);
        for (String subSvcId : subServices) {
            doStartStop(subSvcId, bStart);
        }
    }

    private static List<String> getSubservices(String svc) {
        if(!EP_ServiceManager.class.getName().equals(svc))
            return Collections.EMPTY_LIST;
        PKEPServices ss = getServices();
        ArrayList<String> res = new ArrayList<String>(ss.listRegisteredServices());
        res.remove(EP_ServiceManager.class.getName());
        return res;
    }

    private static PKEPServices getServices() {
        IBasicServer rep = ApiStack.getInterface(IBasicServer.class);
        return (PKEPServices) rep.getExtensionPoint(PKEPServices.class);
	}
    
    public static void doStartStop(long id, boolean bStart) throws EServiceNotFound {
    	PKEPServices ss = getServices();
        String svc = ss.getServiceId(id);
        doStartStop(svc, bStart);
    }

    public static void doStartStop(String svcName, boolean bStart) {
        PKEPServices ss = getServices();
        EP_IService svc = ss.getService(svcName);
        ISrvSmartTranFactory.Helper.commitAndStartTran();
        if (bStart) {
            svc.start();
            doStartStopSubservices(svcName, bStart);
        } else {
            doStartStopSubservices(svcName, bStart);
            if (EP_ServiceManager.class.getName().equals(svc)) {
                ApiAlgs.getLog(EP_ServiceManager.class).trace(
                        "Stopping Service Manager ..");//$NON-NLS-1$
            }
            svc.stop();
        }

    }

    @Override
    public void doCycle() throws Throwable {
        StartStopServiceCmd cmd = (StartStopServiceCmd) m_item;
        ISrvSmartTranFactory.Helper.commitAndStartTran();
        doStartStop(cmd.id, cmd.start);
    }

    @Override
    public void initCycle() {
        super.initCycle();
    }

}
