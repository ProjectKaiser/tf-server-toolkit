/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.triniforce.soap.PropertiesSequence;

@PropertiesSequence( sequence = {"not"})
public abstract class WhereExpr {

    
    private boolean m_not;
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    abstract public Set<String> calcColumnNames();
    
    @PropertiesSequence( sequence = {"colExprs"}) 
    public static class ExprColumnOr extends WhereExpr{
        private List<ColumnExpr> m_colExprs = new ArrayList<ColumnExpr>();

        public List<ColumnExpr> getColExprs() {
            return m_colExprs;
        }
        public void setColExprs(List<ColumnExpr> colExprs) {
            m_colExprs = colExprs;
        }
        
        @Override
        public Set<String> calcColumnNames() {
            Set<String> res = new HashSet<String>();
            for(ColumnExpr e:m_colExprs){
                res.addAll(e.calcColumnNames());
            }
            return res;

        }
    }
    
    /**
     * Applies lucene query to text fields with appropriate weight.
     * E.q. to title and descr.
     */
    @PropertiesSequence( sequence = {"query"})
    public static class TextFieldsQuery extends WhereExpr{
        String m_query;

        public String getQuery() {
            return m_query;
        }

        public void setQuery(String query){
            m_query = query;
        }
        
        @Override
        public Set<String> calcColumnNames() {
            Set res = new HashSet<String>();
            res.add("name");
            res.add("descr");
            return res;
        }
        
    }
    
    @PropertiesSequence( sequence = {"columnName"})
    public static abstract class ColumnExpr extends WhereExpr{
        String m_colName;
        
        public ColumnExpr() {
        }
        
        
        public ColumnExpr(String columnName) {
            m_colName = columnName;
        }
        
        public String getColumnName() {
            return m_colName;
        }
        public void setColumnName(String columnName) {
            m_colName = columnName;
        }
        
        @Override
        public Set<String> calcColumnNames() {
            Set res = new LinkedHashSet();
            res.add(m_colName);
            return res;
        }
    }
    
    @PropertiesSequence( sequence = {"value"})
    public static abstract class ColumnExprValued extends ColumnExpr{
        private Object m_value;
        
        public Object getValue() {
            return m_value;
        }
        public void setValue(Object value) {
            m_value = value;
        }
        
        public ColumnExprValued() {
        }
        
        public ColumnExprValued(String name, Object value){
            setColumnName(name);
            m_value = value;
        }
        
        @Override
        public Set<String> calcColumnNames() {
            Set res = super.calcColumnNames();
            if(m_value instanceof ExprV){
                res.addAll(((ExprV)m_value).calcColumnNames());                
            }
            return res;
        }
        
    }
    
    
    @PropertiesSequence( sequence = {"value"})
    public static class ExprNotNull extends ColumnExpr{
        public ExprNotNull() {
        }
        public ExprNotNull(String columnName) {
            super(columnName);
        }
    }
    
    @PropertiesSequence( sequence = {"from", "to"})
    public static class ExprBetween extends ColumnExpr{
        Object m_from;
        Object m_to;
        public Object getFrom() {
            return m_from;
        }
        public void setFrom(Object from) {
            m_from = from;
        }
        public Object getTo() {
            return m_to;
        }
        public void setTo(Object to) {
            m_to = to;
        }
        
        public ExprBetween() {
        }
        public ExprBetween(String colName, Object from, Object to) {
            setColumnName(colName);
            setFrom(from);
            setTo(to);
        }
        
    }
    
    public static class ExprContains  extends ColumnExprValued{
        public ExprContains() {
        }
        
        public ExprContains(String name, Object value) {
            super(name, value);
        }
    }
    
    public static class ExprEquals  extends ColumnExprValued{
        public ExprEquals() {
        }
        
        public ExprEquals(String name, Object value) {
            super(name, value);
        }
    }
    
    public static class ExprGT  extends ColumnExprValued{
        public ExprGT() {
        }
        
        public ExprGT(String name, Object value) {
            super(name, value);
        }
    }
    
    public static class ExprGE  extends ColumnExprValued{
        public ExprGE() {
        }
        
        public ExprGE(String name, Object value) {
            super(name, value);
        }
    }
    
    public static class ExprLE  extends ColumnExprValued{
        public ExprLE() {
        }
        
        public ExprLE(String name, Object value) {
            super(name, value);
        }
    }
    
    public static class ExprLT  extends ColumnExprValued{
        public ExprLT() {
        }
        
        public ExprLT(String name, Object value) {
            super(name, value);
        }
    }
    
    public static class ExprContainsWord  extends ColumnExprValued{
        public ExprContainsWord() {
        }
        public ExprContainsWord(String name, Object value){
            super(name, value);
        }
    }
    
    
    
    @PropertiesSequence( sequence = {"vals"})
    public static class ExprIn extends ColumnExpr{
        Object m_vals[];
        public Object[] getVals() {
            return m_vals;
        }
        public void setVals(Object[] m_vals) {
            this.m_vals = m_vals;
        }
        
        public ExprIn() {
        }
        
        public ExprIn(String colName, Object[] vals) {
            setColumnName(colName);
            setVals(vals);
        }
        
        @Override
        public String toString() {
            return getColumnName() + " IN " + Arrays.toString(m_vals); 
        }
    }



    public boolean isNot() {
        return m_not;
    }



    public void setNot(boolean not) {
        m_not = not;
    }
}
