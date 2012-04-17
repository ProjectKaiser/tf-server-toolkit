/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.util.List;

import com.triniforce.db.ddl.TableDef.EMetadataException;

public class DropTableOperation extends TableOperation {

	private List<TableUpdateOperation> m_elements;

	public DropTableOperation(List<TableUpdateOperation> elements) {
		m_elements = elements;
	}

	public DropTableOperation() {
		m_elements = null;
	}

	@Override
	public String getName() {
		return "DROP_TABLE"; //$NON-NLS-1$
	}

	@Override
	public TableOperation getReverseOperation() {
		return new CreateTableOperation(m_elements);
	}

	@Override
	public int getVersionIncrease() {
		return m_elements.size();
	}

	@Override
	public void apply(TableDef table) throws EMetadataException {
		throw new EMetadataException(table.getEntityName(), getName());		 //$NON-NLS-1$
	}

}
