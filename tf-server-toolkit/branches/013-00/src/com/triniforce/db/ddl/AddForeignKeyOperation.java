/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 
package com.triniforce.db.ddl;

import java.util.List;

import com.triniforce.db.ddl.TableDef.IndexDef;

/**
 * Adding foreign key to database table
 */
public class AddForeignKeyOperation extends AddIndexOperation{
    
    public enum DELETE_RULE{CASCADE, NOT_SPECIFIED};
    public enum UPDATE_RULE{NOT_SPECIFIED};
    
    private DELETE_RULE m_onDelete;
    private UPDATE_RULE m_onUpdate;
    private List<String> m_refColumns;
    
    private boolean m_bCreateForeignKey= true;
    
    /**
     *  /** Construct command
     * @param name - foreign key name
     * @param columns - columns in table for index
     * @param parentTable - referenced table
     * @param parentIndex - parent table unique index name
     */
    public AddForeignKeyOperation(String name, List<String> columns, String parentTable, String parentIndex){
        this(name, columns, parentTable, parentIndex, DELETE_RULE.NOT_SPECIFIED, UPDATE_RULE.NOT_SPECIFIED);
    }
    
    public AddForeignKeyOperation(String name, List<String> columns, String parentTable, String parentIndex, DELETE_RULE delRule, UPDATE_RULE updateRule) {
        super(IndexDef.foreignKey(name, columns, parentTable, parentIndex));
        m_onDelete = delRule;
        m_onUpdate = updateRule;
    }

    public String getParentTable(){
        return m_index.m_parentTable;
    }
    
    public String getParentIndex(){
        return m_index.m_parentIndex;
    }
    
    DELETE_RULE getOnDeleteRule(){
        return m_onDelete;        
    }
    
    UPDATE_RULE getOnUpdateRule(){
        return m_onUpdate;
    }

    public void setRefColumns(List<String> cols) {
        m_refColumns = cols;
    }

    public List<String> getRefColumns() {
        return m_refColumns;
    }
    
    boolean isCreateFK(){
    	return m_bCreateForeignKey;
    }
    void setCreateFK(boolean value){
    	m_bCreateForeignKey = value;
    }
    
    @Override
    public DeleteIndexOperation getReverseOperation() {
    	if(!m_bCreateForeignKey)
    		return new DeleteIndexOperation(m_index.m_name, IndexDef.TYPE.INDEX, m_index.m_bUnique);
    	return super.getReverseOperation();
    }

}
