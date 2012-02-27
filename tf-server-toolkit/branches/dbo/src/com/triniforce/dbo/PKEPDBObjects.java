/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.dbo;

import com.triniforce.extensions.EExtensionPointNotFound;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.PKExtensionPoint;

public class PKEPDBObjects extends PKExtensionPoint{

	@Override
	public IPKExtension putExtension(String extensionId, Object obj)
			throws EExtensionPointNotFound {
		IDBObject dbo = (IDBObject) obj;
		IPKExtension res = super.putExtension(extensionId, obj);
		for(IDBObject synth : dbo.synthDBObjects()){
			super.putExtension((String) synth.getKey(), synth);
		}
		return res;
	}
	
}
