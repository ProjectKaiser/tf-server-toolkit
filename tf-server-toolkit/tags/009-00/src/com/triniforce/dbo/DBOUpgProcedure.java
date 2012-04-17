/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import com.triniforce.server.srvapi.UpgradeProcedure;

public class DBOUpgProcedure implements IDBObject {
	
	private UpgradeProcedure m_proc;

	public DBOUpgProcedure(UpgradeProcedure proc) {
		m_proc = proc;
	}

	public Class getActualizerClass() {
		return ExDBOAUpgradeProcedures.class;
	}

	public IDBObject[] getDependiencies() {
		return new IDBObject[]{};
	}

	public Object getKey() {
		return getProc().getEntityName();
	}

	public IDBObject[] synthDBObjects() {
		return new IDBObject[]{};
	}

	public UpgradeProcedure getProc() {
		return m_proc;
	}

}
