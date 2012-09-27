/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.db.dml;

import java.util.List;
import java.util.Map;

import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;


public interface ISmartTran extends IStmtContainer {

    /**
     * Mark transaction to be commited 
     */
    void commit();
    
    void close(boolean bCommit);
    
    /**
     * Transaction is not commited even if commit() method was called 
     */
    void doNotCommit();    
    
    /**
     * @return true is transaction will be commited, false otherwise. Transaction will be 
     * commited if commit() has been called and doNotCommit() has not been called.
     */
    boolean toBeCommited();
    
    boolean isCommited();
    
    void insert(Class table, IName fields[], Object values[]);
    void insert(Class table, List<IName> fields, List<Object> values);
    void insert(Class table, Map<IName, Object> values);
    void update(Class table, IName fields[], Object values[], IName lookUpFields[], Object lookUpValues[]);
    void update(Class table, List<IName> fields, List<Object> values, List<IName> lookUpFields, List<Object> lookUpValues);
    void update(Class table, Map<IName, Object> values, Map<IName, Object> lookUpValues);
	ResSet select(Class table, IName fields[],
	            IName lookUpFields[], Object lookUpValues[]);
    ResSet select(Class table, IName fields[],
            IName lookUpFields[], Object lookUpValues[], IName orderByFields[]);
    
    void delete(Class table, IName lookUpFields[], Object lookUpValues[]);
    void delete(Class table, Map<IName, Object> lookUpValues);
    
    public <T> T instantiateBL(Class<? extends T> cls);    
    
    public static class Helper{
        @SuppressWarnings("unchecked")
        public static <T> T instantiateBL(Class<? extends T> cls){
            return ApiStack.getInterface(ISrvSmartTran.class).instantiateBL(cls);
        }
    }
    
    public static class DescName implements IName{
        private final String m_name;
        
        public DescName(String name){
            m_name = name;
            
        }
        
        public String getName(){
            return m_name;
        }
        
    }
    
    public static class Between{
        private Object leftValue;
        private Object rightValue;
        
        public Between(Object a_leftValue, Object a_rightValue) {
            leftValue = a_leftValue;
            rightValue = a_rightValue;
        }
        public void setLeftValue(Object leftValue) {
            this.leftValue = leftValue;
        }
        public Object getLeftValue() {
            return leftValue;
        }
        public void setRightValue(Object rightValue) {
            this.rightValue = rightValue;
        }
        public Object getRightValue() {
            return rightValue;
        }
        
    }
    

}