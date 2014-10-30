/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

/**
 * Single- or multiple instances depending on isSingle() property
 */
public class ObjectInstantiator implements ISimpleObjectFactory{
    
    Object m_instance;
    private final ISimpleObjectFactory m_of;
    private boolean m_single;
    
    ObjectInstantiator(ISimpleObjectFactory od){
        m_of = od;
    }
    
    public boolean isSingle(){
        return m_single;
    }
    
    public boolean isNewInstance(){
        if(isSingle()){
            if(null == m_instance){
                return true;               
            }
            return false;
            
        } else {
            return true;
        }        
    }
    
    /**
     * @return new/same instance each time depending on isSingle
     */
    public Object getInstance(){
        if(isNewInstance()){
            m_instance = m_of.getInstance();            
        }
        return m_instance;
    }

    public void setSingle(boolean single) {
        m_single = single;
    }

    public Class getObjectClass() {
        return m_of.getObjectClass();
    }
}
