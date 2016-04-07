/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import com.triniforce.server.plugins.kernel.BasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.soap.MultiClassServiceInvoker;
import com.triniforce.soap.RequestHandler.IServiceInvoker;

public class BasicServerInvoker implements IServiceInvoker {
	
	private BasicServer m_srv;
	private MultiClassServiceInvoker m_svcs;

	public BasicServerInvoker(BasicServer srv, MultiClassServiceInvoker svcs) {
		m_srv = srv;
		m_svcs = svcs;
		
	}

	@Override
	public Object invokeService(String method, Object... args) {
		m_srv.enterMode(Mode.Running);
		try{
			return m_svcs.invokeService(method, args);
		}finally{
			m_srv.leaveMode();
		}
	}

}
