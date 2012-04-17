/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.utils;

public class Entity implements IEntity{
    
   
    private String m_entityName;
   
    protected void setEntityName(String newName){
        m_entityName = newName;
    }
   
    public Entity(String entityName){
        m_entityName = entityName;        
    }
    public Entity(){
        m_entityName = this.getClass().getName();        
    }    
    
    public String getEntityName() {
        return m_entityName;
    }

}
