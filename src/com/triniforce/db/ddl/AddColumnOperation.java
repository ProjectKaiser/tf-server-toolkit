/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.db.ddl;

import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.utils.TFUtils;

/**
 * Adding column to database table
 */
public class AddColumnOperation extends TableUpdateOperation<FieldDef> {
    
    protected FieldDef m_field;
    
    /**
     * @param field - operation creates this
     */
    public AddColumnOperation(FieldDef field) {
        super();
        m_field = field;  
    }
    
    /* (non-Javadoc)
     * @see com.triniforce.db.ddl.IUpdate#apply(com.triniforce.db.ddl.DBTable)
     */
    public void apply(TableDef table) throws EMetadataException {
        table.getFields().addElement(m_field);
    }
    
    /**
     * @return field, operation creates
     */
    public FieldDef getField(){
        return m_field;
    }

    /* (non-Javadoc)
     * @see com.triniforce.db.ddl.IUpdate#getName()
     */
    public String getName() {
        return m_field.m_name;
    }

	/* (non-Javadoc)
	 * @see com.triniforce.db.ddl.TableOperation#getReverseOperation()
	 */
	public TableUpdateOperation getReverseOperation() {
		return new DeleteColumnOperation(m_field);
	}
	
	@Override
	public String toString() {		
		return String.format("%s", m_field.toString());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AddColumnOperation))
			return false;
		AddColumnOperation other = (AddColumnOperation) obj;
		
		return m_field.equals(other.m_field);
	}


}
