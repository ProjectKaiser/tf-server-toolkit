/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.security.InvalidParameterException;

import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.Fields;

public class AlterColumnOperation extends TableUpdateOperation {
    FieldDef m_oldField;
    FieldDef m_newField;
	private boolean m_bSetType;
    
    public AlterColumnOperation(FieldDef oldField, FieldDef newField) {
        if(!oldField.m_name.equals(newField.m_name))
            throw new TableDef.EInvalidDefinitionArgument("name"); //$NON-NLS-1$
        m_oldField = oldField;
        m_newField = newField;
        m_bSetType = !(m_oldField.m_type.equals(m_newField.m_type) && 
                m_oldField.m_size==m_newField.m_size &&
                m_oldField.m_scale==m_newField.m_scale);
        
        if(!(bSetType() || bSetNotNullFlag()))
            throw new InvalidParameterException();
    }

    @Override
    public void apply(TableDef table) throws EMetadataException {
    	if(bSetType()){
    		Fields fs = table.getFields();
    		fs.findElement(getNewField().getName()).getElement().m_type = getNewField().getType();
    	}
    	return ;
//        throw new EMetadataException(table==null ? null : table.getEntityName(), null);         //$NON-NLS-1$
    }

    @Override
    public String getName() {
        return m_newField.m_name;
    }

    @Override
    public TableOperation getReverseOperation() {
        return new AlterColumnOperation(m_newField, m_oldField);
    }

    public boolean bSetType() {
        return m_bSetType;
    }

    public boolean bSetNotNullFlag() {
        return true;
    }

    public FieldDef getNewField() {
        return m_newField;
    }
}
