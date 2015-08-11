/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import com.triniforce.db.ddl.TableDef.EMetadataException;

public class EmptyCommand extends TableUpdateOperation {

	@Override
	public void apply(TableDef table) throws EMetadataException {
	}

	@Override
	public String getName() {
		return "empty";
	}

	@Override
	public TableOperation getReverseOperation() {
		return this;
	}

}
