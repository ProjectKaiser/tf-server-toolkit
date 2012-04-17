/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import com.triniforce.db.ddl.TableDef;

public class DBOTabDef implements IDBObject {
	
	private TableDef m_def;

	public DBOTabDef(TableDef td) {
		m_def = td;
	}

	public Class getActualizerClass() {
		return ExDBOATables.class;
	}

	public IDBObject[] getDependiencies() {
		return new IDBObject[]{};
	}

	public Object getKey() {
		return m_def.getEntityName();
	}

	public IDBObject[] synthDBObjects() {
		return new IDBObject[]{};
	}

	public TableDef getDef() {
		return m_def;
	}

}
