/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.util.Collections;
import java.util.List;

import com.triniforce.db.ddl.TableDef.EMetadataException;

public class CreateTableOperation extends TableOperation {
	
	String m_dbName=null;
	
	private List<TableUpdateOperation> m_elements;

	public CreateTableOperation(List<TableUpdateOperation> elements) {
		m_elements = elements;
	}

	@Override
	public String getName() {
		return "CREATE_TABLE"; //$NON-NLS-1$
	}

	@Override
	public TableOperation getReverseOperation() {
		return new DropTableOperation(m_elements);
	}

	@Override
	public int getVersionIncrease() {
		return m_elements.size();
	}

	public List<TableUpdateOperation> getElements() {
		return Collections.unmodifiableList(m_elements);
	}

	@Override
	public void apply(TableDef table) throws EMetadataException {
		throw new EMetadataException(table.getEntityName(), null);		 //$NON-NLS-1$
	}

	public String getDbName() {
		return m_dbName;
	}

	public void setDbName(String dbName) {
		m_dbName = dbName;
	}

}
