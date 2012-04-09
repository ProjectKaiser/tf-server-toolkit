/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;



import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.IndexDef.TYPE;

public class DeleteIndexOperation extends TableUpdateOperation<IndexDef> {

    String m_constrName;
    IndexDef.TYPE m_type;
    TableDef m_opTable;
	private boolean m_bUnique;
    
    public DeleteIndexOperation(String constrName, IndexDef.TYPE t, boolean bUnique) {
        m_constrName = constrName;
        m_type = t;
        m_bUnique = bUnique;
    }

	public void apply(TableDef table) throws EMetadataException {
        int idx = table.getIndices().getPosition(m_constrName);
        if(idx < 0)
            throw new EMetadataException( table.getEntityName(), m_constrName); //$NON-NLS-1$
        table.getIndices().removeElement(idx);
        m_opTable = table;
    }

    public String getName() {
        return m_constrName;
    }

	public TableUpdateOperation getReverseOperation() {
		TableUpdateOperation op = null;
        
        if(m_opTable!=null){
            int v = m_opTable.getIndices().find(m_opTable.getIndices().getAddedElements().iterator(), new TableDef.IElementDef.NameCondition<IndexDef>(m_constrName)).getVersion();
            op = m_opTable.getHistory(v).get(0);
        }

		return op;
	}

    public TYPE getType() {
        return m_type;
    }
    
    public boolean isUniqueIndex(){
    	return m_bUnique;
    }

}
