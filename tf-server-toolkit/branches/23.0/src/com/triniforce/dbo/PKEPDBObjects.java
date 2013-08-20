/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.dbo;

import com.triniforce.extensions.EExtensionPointNotFound;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.utils.ApiStack;

public class PKEPDBObjects extends PKExtensionPoint{
	
	public PKEPDBObjects() {
		setExtensionClass(IDBObject.class);
	}

	@Override
	public IPKExtension putExtension(String extensionId, Object obj)
			throws EExtensionPointNotFound {
		ApiStack.pushInterface(PKEPDBObjects.class, this);
		try{
    		IPKExtension res = super.putExtension(extensionId, obj);
    		IDBObject dbo = res.getInstance();
    		for(IDBObject synth : dbo.synthDBObjects()){
    			super.putExtension((String) synth.getKey(), synth);
    		}
    		return res;
		}finally{
		    ApiStack.popInterface(1);		    
		}
	}
	
}
