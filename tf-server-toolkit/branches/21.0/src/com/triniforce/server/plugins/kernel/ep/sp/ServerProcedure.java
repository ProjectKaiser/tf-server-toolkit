/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.sp;

import com.triniforce.extensions.PKExtension;

public abstract class ServerProcedure extends PKExtension{
	protected abstract Object invoke(Object... arguments);
	
	public boolean requireRunningMode(){return true;};
}