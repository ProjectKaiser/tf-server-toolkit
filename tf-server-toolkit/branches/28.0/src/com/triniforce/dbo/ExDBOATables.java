/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.dbo;

import java.util.List;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class ExDBOATables extends DBOActualizer {

	public ExDBOATables() {
		super(true, Mode.Registration);
	}
	
	@Override
	public void actualize(List<IDBObject> dboList) {
		ISORegistration reg = ApiStack.getInterface(ISORegistration.class);
		for(IDBObject dbo : dboList){
	        TableDef td = (TableDef)dbo;
	        reg.registerTableDef(td);
		}
	}

}
