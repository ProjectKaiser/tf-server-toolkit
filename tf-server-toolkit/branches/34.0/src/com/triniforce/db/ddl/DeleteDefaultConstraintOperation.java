/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;


public class DeleteDefaultConstraintOperation extends TableOperation {
    
    String m_constraintName;
    String m_columnName;
    Object m_value;

    public DeleteDefaultConstraintOperation(String defCnstr, String colName, Object value) {
        m_constraintName = defCnstr;
        m_columnName = colName;
        m_value = value;
    }

    @Override
    public String getName() {
        return m_constraintName;
    }

    @Override
    public TableOperation getReverseOperation() {
        return new AddDefaultConstraintOperation(m_constraintName, m_columnName, m_value);
    }

    @Override
    public int getVersionIncrease() {
        return 0;
    }
    
    public String getColumnName(){
        return m_columnName;
    }

}
