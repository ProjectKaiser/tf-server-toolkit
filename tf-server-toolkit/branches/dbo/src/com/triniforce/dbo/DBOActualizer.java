/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.dbo;

import java.util.List;

import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IBasicServer.Mode;

public class DBOActualizer {

	private boolean m_hasOwnVersioning;
	private Mode m_mode;

	public DBOActualizer(boolean hasOwnVersioning, IBasicServer.Mode mode) {
		m_hasOwnVersioning = hasOwnVersioning;
		m_mode = mode;
	}
	
	public boolean hasOwnVersioning(){
		return m_hasOwnVersioning;
	}
	
	public void actualize(List<IDBObject> dboList){
		
	}
	
	public IBasicServer.Mode getAtualizationMode(){
		return m_mode;
	}
}
