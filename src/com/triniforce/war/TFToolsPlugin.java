/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.server.TFPlugin;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIs;


public class TFToolsPlugin extends TFPlugin {
	
	List<Object> m_svcs = new ArrayList<Object>();

	@Override
	public void doRegistration() {
		
	}

	@Override
	public void doExtensionPointsRegistration() {
		putExtensionPoint(new com.triniforce.war.UEPServiceEndoint());
		
		for (Object svc : m_svcs) {
			putExtension(PKEPAPIs.class, svc.getClass().getName(), svc);
		}
		
		putExtension(UEPServiceEndoint.class, BeanShellExecutor.class);

	}
	
	

	public void addServiceExtension(Object svc) {
		m_svcs.add(svc);
		
	}

}
