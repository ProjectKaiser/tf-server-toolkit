/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.db.dml;

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
    void update(Class table, IName fields[], Object values[], IName lookUpFields[], Object lookUpValues[]);
	ResSet select(Class table, IName fields[],
	            IName lookUpFields[], Object lookUpValues[]);
    ResSet select(Class table, IName fields[],
            IName lookUpFields[], Object lookUpValues[], IName orderByFields[]);
    
    void delete(Class table, IName lookUpFields[], Object lookUpValues[]);
    
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

}