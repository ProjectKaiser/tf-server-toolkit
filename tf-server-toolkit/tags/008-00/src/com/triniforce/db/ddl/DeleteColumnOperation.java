/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;


import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;

public class DeleteColumnOperation extends TableUpdateOperation<FieldDef> {
    
    private String m_colName;
    private FieldDef m_deletedField;

    public DeleteColumnOperation(String colName){
        m_colName = colName;
    }
    
    public DeleteColumnOperation(FieldDef field) {
    	m_deletedField = field;
    	m_colName = field.m_name;
	}

	public void apply(TableDef table) throws EMetadataException {
        int iField = table.getFields().getPosition(m_colName);
        if(iField == -1)
            throw new EMetadataException(table.getEntityName(), m_colName); //$NON-NLS-1$
        m_deletedField = table.getFields().removeElement(iField);
    }

    public String getName() {
        return m_colName;
    }

	public TableUpdateOperation getReverseOperation() {	
		return new AddColumnOperation(m_deletedField);
	}
	
	public FieldDef getDeletedField(){
		return m_deletedField;
	}

}
