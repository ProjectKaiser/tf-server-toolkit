/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

public class AddDefaultConstraintOperation extends TableOperation {

    String m_constraintName;
    String m_columnName;
    Object m_value;
    
    public AddDefaultConstraintOperation(String name, String colName, Object value) {
        m_constraintName = name;
        m_columnName = colName;
        m_value = value;
    }

    @Override
    public String getName() {
        return m_constraintName;
    }

    @Override
    public TableOperation getReverseOperation() {
        return new DeleteDefaultConstraintOperation(m_constraintName, m_columnName, m_value);
    }

    @Override
    public int getVersionIncrease() {
        return 0;
    }
    
    public String getColumnName(){
        return m_columnName;
    }

    public Object getValue() {
        return m_value;
    }
}
