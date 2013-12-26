/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel;

import com.triniforce.server.plugins.kernel.tables.NextId;
import com.triniforce.server.srvapi.IMiscIdGenerator;

public class MiscIdGenerator extends IdGenerator implements IMiscIdGenerator{

	public MiscIdGenerator(int NumCacheSize, NextId genDef) {
		super(NumCacheSize, genDef);
	}

}
