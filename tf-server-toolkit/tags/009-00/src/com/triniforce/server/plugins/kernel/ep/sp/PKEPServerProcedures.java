/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.sp;

import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiStack;


public class PKEPServerProcedures extends PKExtensionPoint{
	
	public PKEPServerProcedures() {
		setExtensionClass(ServerProcedure.class);
	}
	
	public Object invokeProcedure(Boolean aEnterRunningMode, String procedureId,
			Object... args) {
		ServerProcedure sp = getExtension(procedureId).getInstance();
		IBasicServer bs = (IBasicServer) getRootExtensionPoint();
		boolean enterRunningMode = null != aEnterRunningMode ? aEnterRunningMode: sp.requireRunningMode();
		if (enterRunningMode) {
			bs.enterMode(IBasicServer.Mode.Running);
		} else {
			ApiStack.pushApi(bs.getCoreApi());
		}
		try {
			return sp.invoke(args);
		} finally {
			if (enterRunningMode) {
				bs.leaveMode();
			} else {
				ApiStack.popApi();
			}
		}

	}

	public Object invokeProcedure(String procedureId, Object... args) {
		return invokeProcedure(null, procedureId, args);
	}
	
}
