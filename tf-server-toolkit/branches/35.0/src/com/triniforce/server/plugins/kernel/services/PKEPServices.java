/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.triniforce.extensions.EExtensionPointNotFound;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.ApiAlgs;

public class PKEPServices extends PKExtensionPoint{
	
    public PKEPServices() {
        setExtensionClass(IService.class);
        setWikiDescription("Services");
    }
    
	public static class EServiceNotFound extends Exception{
		private static final long serialVersionUID = 5798098277505044559L;
		public EServiceNotFound(long id) {
			super("Service: " + id);
		}
	}

    Map<Long, String> m_registeredServices = new HashMap<Long, String>();
    Map<Long, IService> m_services = new HashMap<Long, IService>();
	private Long m_serviceManagerId;

	
    public synchronized IService getService(long id) throws EServiceNotFound{
        IService res = m_services.get(id);
        if (null != res)
            return res;

        String svcId = getServiceId(id);

        Service svc = (Service) getService(svcId);
        m_services.put(id, svc);
        return svc;
        
    }
	
	public Collection<String> listRegisteredServices() {
        return getExtensions().keySet();
    }
    
    public synchronized IService getService(String svcName){
        Service svc = getExtension(svcName).getInstance();
        return svc;        
    }
    
    public String getServiceId(long id) throws EServiceNotFound {
        String res = m_registeredServices.get(id);
        if(null == res){
        	HashSet<String> keys = new HashSet<String>(getExtensions().keySet());
        	keys.removeAll(m_registeredServices.values());
        	for(String key : keys){
        		IService svc = getExtension(key).getInstance();
        		if(svc instanceof EP_QueuedService){
        			long svcId = ((EP_QueuedService)svc).getId();
        			if(id == svcId){
        				res = key;
        				break;
        			}
        		}
        	}
        	if(null == res)
        		throw new EServiceNotFound(id);
        	else
        		registerService(id, res);
        }
        return res;
    }
    
    private void registerServiceManager(EP_ServiceManager sm){
    	m_serviceManagerId = sm.getId(); 
//    	registerService(m_serviceManagerId, sm.getClass().getName());
//    	putExtension(sm.getClass().getName(), sm);
    }
    
    private void registerService(long id, String svcId){
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
	
	@Override
	public IPKExtension putExtension(String extensionId, Object obj)
			throws EExtensionPointNotFound {
		if(obj instanceof EP_ServiceManager){
			registerServiceManager((EP_ServiceManager) obj);
		}
		if(obj instanceof EP_QueuedService){
			EP_QueuedService qSrv = (EP_QueuedService)obj;
			registerService(qSrv.getId(), qSrv.getName());
		}
		return super.putExtension(extensionId, obj);
		
	}
	
}
