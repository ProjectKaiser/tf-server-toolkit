/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.srv_ev;

import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.IBasicServer.Mode;

public enum ServerEvent{PLUGINS_REGISTRATION_FINISHED(false), SERVER_INIT(true), SERVER_FINIT(true);
	private final boolean m_inRunningMode;
	ServerEvent(boolean inRunningMode){
		m_inRunningMode = inRunningMode;
	}
	public void init(IBasicServer srv){
		if(m_inRunningMode){
			srv.enterMode(Mode.Running);
		}
	}
	public void finit(IBasicServer srv){
		if(m_inRunningMode){
    		ISrvSmartTranFactory.Helper.commit();
			srv.leaveMode();
		}
	}
}