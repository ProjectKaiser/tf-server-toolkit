/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.util.List;

import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.IndexDef;

public class AddIndexOperation extends TableUpdateOperation {

    protected IndexDef m_index;
    boolean m_bDbName;

    public AddIndexOperation(IndexDef index) {
        m_index = index;
        m_bDbName = false;
    }

    @Override
    public void apply(TableDef table) throws EMetadataException {
        table.getIndices().addElement(m_index);
    }

    @Override
    public String getName() {
        return m_index.getName();
    }
    
    public List<String> getColumns(){
        return m_index.m_columns;
    }

    @Override
    public DeleteIndexOperation getReverseOperation() {
        return new DeleteIndexOperation(m_index.m_name, m_index.m_type, m_index.m_bUnique);
    }
    
    public IndexDef getIndex(){
        return m_index;
    }

    public boolean isUnique() {
        return m_index.m_bUnique;
    }

    public boolean isAscending() {
        return m_index.m_bAscending;
    }
    
    public boolean isDbIndexName(){
    	return m_bDbName;
    }

    public void setIsDbIndexName(boolean value){
    	m_bDbName = value;
    }

	public boolean isClustered() {
		return m_index.m_bClustered;
	}
	
	@Override
	public String toString() {		
		return String.format("%s", m_index.toString());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AddIndexOperation))
			return false;
		AddIndexOperation other = (AddIndexOperation) obj;
		
		return m_index.equals(other.m_index);
	}
}
