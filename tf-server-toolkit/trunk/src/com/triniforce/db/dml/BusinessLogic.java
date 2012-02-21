/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.dml;

import com.triniforce.utils.IName;

/**
 * Business-logic object
 */
public abstract class BusinessLogic {
    
    private ISmartTran m_st;

    public ISmartTran getSt() {
        return m_st;
    }

    public void setSt(ISmartTran st) {
        m_st = st;
    }
    
    public abstract Class getTable();
    
    protected void insert(IName fields[], Object values[]){
        m_st.insert(getTable(), fields, values);
    }
    
    protected ResSet select(IName fields[],
                IName lookUpFields[], Object lookUpValues[]){
        return m_st.select(getTable(), fields, lookUpFields, lookUpValues);
    }
 
    protected void update(IName fields[], Object values[],
            IName lookUpFields[], Object lookUpValues[]){
        m_st.update(getTable(), fields, values, lookUpFields, lookUpValues);
    }    
    
    protected ResSet select(IName fields[],
            IName lookUpFields[], Object lookUpValues[], IName orderByFields[]){
        return m_st.select(getTable(), fields, lookUpFields, lookUpValues, orderByFields);
    }
    
    protected void delete(IName lookUpFields[], Object lookUpValues[]){
    	m_st.delete(getTable(), lookUpFields, lookUpValues);
    }
    
}
