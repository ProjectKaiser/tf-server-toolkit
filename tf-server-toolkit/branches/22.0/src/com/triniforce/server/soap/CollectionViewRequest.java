/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.IName;

@PropertiesSequence( sequence = {"target", "parentOf", "parentId", "columns", 
        "where", "orderBy", "whereExprs", "functions", "dbValue"}) 
public class CollectionViewRequest extends LongListRequest {

    Long m_parentOf;
    
    
    public Long getParentOf() {
        return m_parentOf;
    }

    public void setParentOf(Long parentOf) {
        m_parentOf = parentOf;
    }
    
    @PropertiesSequence( sequence = {"stackTrace"})
    public static class EParentIdMustHaveValue extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EParentIdMustHaveValue() {
            super("parentId parameter must have value");//$NON-NLS-1$
        }
    }
    
    
    /**
     * Used for order by desc
     */
    @PropertiesSequence( sequence = {"field"})
    public static class DescField{
        protected String m_field;
        public String getField() {
            return m_field;
        }
        public void setField(String field) {
            m_field = field;
        }
        public DescField(String field){
            m_field = field;
        }
        public DescField(){
            m_field = null;
        }        
    }
    
    protected String m_target;

    Long m_parentId = 3L;

    List<String> m_columns = new ArrayList<String>();

    Map<String, Object> m_where = new HashMap<String, Object>();

    List<Object> m_orderBy = new ArrayList<Object>();
    
    List<WhereExpr> m_whereExprs = new ArrayList<WhereExpr>();
    
    List<FieldFunctionRequest> m_functions = new ArrayList<FieldFunctionRequest>();
    
    boolean m_bDbValue = false;

    public List<String> getColumns() {
        return m_columns;
    }

    public void setColumns(List<String> columns) {
        m_columns = columns;
    }
    
    public List<Object> getOrderBy() {
        return m_orderBy;
    }

    public void setOrderBy(List<Object> orderBy) {
        m_orderBy = orderBy;
    }

    public Long getParentId() {
        return m_parentId;
    }

    public void setParentId(Long parentId) {
        m_parentId = parentId;
    }

    public String getTarget() {
        return m_target;
    }
    
    public  void setTargetCls(Class cls){
        setTarget(cls.getName());
    }
    
    public void addColumn(IName col){
        getColumns().add(col.getName());
    }
    public void addColumn(String col){
        getColumns().add(col);
    }
    
    public void addWhere(IName col, Object value){
        getWhere().put(col.getName(), value);
    }
    
    public void addWhere(String col, Object value){
        getWhere().put(col, value);
    }

    public void setTarget(String target) {
        m_target = target;
    }

    public Map<String, Object> getWhere() {
        return m_where;
    }

    public void setWhere(Map<String, Object> where) {
        m_where = where;
    }

    public List<FieldFunctionRequest> getFunctions() {
        return m_functions;
    }

    public void setFunctions(List<FieldFunctionRequest> functions) {
        m_functions = functions;
    }

	public boolean isDbValue() {
		return m_bDbValue;
	}

	public void setDbValue(boolean dbValue) {
		this.m_bDbValue = dbValue;
	}

    public List<WhereExpr> getWhereExprs() {
        return m_whereExprs;
    }

    public void setWhereExprs(List<WhereExpr> whereExprs) {
        m_whereExprs = whereExprs;
    }
}
