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

/**
 * Business-logic object
 */
public abstract class BusinessLogic {
    
    private ISmartTran m_st_;

    public ISmartTran getSt() {
        if( null == m_st_){
            return ApiStack.getInterface(ISrvSmartTran.class);
        }
        return m_st_;
    }

    public void setSt(ISmartTran st) {
        m_st_ = st;
    }
    
    public abstract Class getTable();

    protected void insert(List<IName> fields, List<Object> values){
        getSt().insert(getTable(), fields, values);
    }
    
    protected void insert(Map<IName, Object> values){
    	getSt().insert(getTable(), values);
    }

    
    protected void insert(IName fields[], Object values[]){
        getSt().insert(getTable(), fields, values);
    }
    
    protected ResSet select(IName fields[],
                IName lookUpFields[], Object lookUpValues[]){
        return getSt().select(getTable(), fields, lookUpFields, lookUpValues);
    }
 
    protected void update(IName fields[], Object values[],
            IName lookUpFields[], Object lookUpValues[]){
        getSt().update(getTable(), fields, values, lookUpFields, lookUpValues);
    }    
    
    protected void update(Map<IName, Object> values, Map<IName, Object> lookUp){
    	getSt().update(getTable(), values, lookUp);
    }
    
    protected ResSet select(IName fields[],
            IName lookUpFields[], Object lookUpValues[], IName orderByFields[]){
        return getSt().select(getTable(), fields, lookUpFields, lookUpValues, orderByFields);
    }
    
    protected void delete(IName lookUpFields[], Object lookUpValues[]){
        getSt().delete(getTable(), lookUpFields, lookUpValues);
    }
    protected void delete(Map<IName, Object> lookUpValues){
        getSt().delete(getTable(), lookUpValues);
    }

}
