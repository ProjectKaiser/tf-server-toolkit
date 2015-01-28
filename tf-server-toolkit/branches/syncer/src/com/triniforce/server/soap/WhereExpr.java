/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.triniforce.soap.PropertiesSequence;

@PropertiesSequence( sequence = {"not"})
public class WhereExpr {
    
    private boolean m_not;
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    
    @PropertiesSequence( sequence = {"colExprs"}) 
    public static class ExprColumnOr extends WhereExpr{
        private List<ColumnExpr> m_colExprs = new ArrayList<ColumnExpr>();

        public List<ColumnExpr> getColExprs() {
            return m_colExprs;
        }
        public void setColExprs(List<ColumnExpr> colExprs) {
            m_colExprs = colExprs;
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
        
    }
    
    @PropertiesSequence( sequence = {"columnName"})
    public static abstract class ColumnExpr extends WhereExpr{
        String m_columnName;
        public String getColumnName() {
            return m_columnName;
        }
        public void setColumnName(String columnName) {
            m_columnName = columnName;
        }        
    }
    
    @PropertiesSequence( sequence = {"value"})
    public static abstract class ColumnExprValued extends ColumnExpr{
        String m_columnName;
        private Object m_value;
        public String getColumnName() {
            return m_columnName;
        }
        public void setColumnName(String columnName) {
            m_columnName = columnName;
        }
        public Object getValue() {
            return m_value;
        }
        public void setValue(Object value) {
            m_value = value;
        }
        
        public ColumnExprValued() {
        }
        
        public ColumnExprValued(String name, Object value){
            m_columnName = name;
            m_value = value;
        }
        
    }
    
    
    @PropertiesSequence( sequence = {"value"})
    public static class ExprNotNull extends ColumnExpr{
        private Object m_value;

        public void setValue(Object value){
            m_value = value;
        }
        public Object getValue(){
            return m_value;
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
