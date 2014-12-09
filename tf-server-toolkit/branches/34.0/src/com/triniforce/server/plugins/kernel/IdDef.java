/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.utils.IName;

public class IdDef implements IIdDef {
	
	public static final String ID = "ID";

	private FieldDef m_idDef;

	public IdDef(ColumnType columnType) {
		m_idDef = FieldDef.createScalarField(ID, columnType, true);
	}

	public FieldDef getFieldDef() {
		return m_idDef;
	}

	public FieldDef getFieldDef(IName name) {
		return getFieldDef(name, true);
	}

	public FieldDef getFieldDef(IName name, boolean bNotNull) {
		return FieldDef.createScalarField(name.getName(), m_idDef.getType(), bNotNull);
	}
}