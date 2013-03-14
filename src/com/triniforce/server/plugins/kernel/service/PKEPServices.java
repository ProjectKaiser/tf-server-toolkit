/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiAlgs;

public class PKEPServices extends PKExtensionPoint{
	
	public static class EServiceNotFound extends Exception{
		private static final long serialVersionUID = 5798098277505044559L;
		public EServiceNotFound(long id) {
			super("Service: " + id);
		}
	}

    Map<Long, String> m_registeredServices = new HashMap<Long, String>();
    Map<Long, EP_IService> m_services = new HashMap<Long, EP_IService>();
	private Long m_serviceManagerId;

	
    public synchronized EP_IService getService(long id) throws EServiceNotFound{
        EP_IService res = m_services.get(id);
        if (null != res)
            return res;

        String svcId = getServiceId(id);

        EPService svc = (EPService) getService(svcId);
        m_services.put(id, svc);
        return svc;
        
    }
	
	public Collection<String> listRegisteredServices() {
        return getExtensions().keySet();
    }
    
    public synchronized EP_IService getService(String svcName){
        EPService svc = getExtension(svcName).getInstance();
        return svc;        
    }
    
    public String getServiceId(long id) throws EServiceNotFound {
        String res = m_registeredServices.get(id);
        if(null == res)
            throw new EServiceNotFound(id);
        return res;
    }
    
    public void registerServiceManager(EP_ServiceManager sm){
    	m_serviceManagerId = sm.getId(); 
    	registerService(m_serviceManagerId, sm.getClass().getName());
    	putExtension(sm.getClass().getName(), sm);
    }
    
    public void registerService(long id, String svcId){
        ApiAlgs.assertTrue(null == m_registeredServices.put(id, svcId), svcId);
    }
    
    public void startStopWithSubservices(long serviceId, boolean bStart) {
        ISrvSmartTranFactory.Helper.push();
        try{
            EP_ServiceManager.StartStopServiceCmd cmd = new EP_ServiceManager.StartStopServiceCmd();
            cmd.id = serviceId;
            cmd.start = bStart;            
            IDbQueueFactory.Helper.getQueue(m_serviceManagerId).put(cmd);
            ISrvSmartTranFactory.Helper.commit();            
        }
        finally{
            ISrvSmartTranFactory.Helper.pop();
        }
    }
	
	public long getServiceManagerId(){
		ApiAlgs.assertNotNull(m_serviceManagerId, "Service manager are not registered");
		return m_serviceManagerId;
	}
	
}
