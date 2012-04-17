/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import java.util.List;

import com.triniforce.utils.IEntity;

/**
 * Query server objects installed by ISORegistration.
 * 
 */

public interface ISOQuery extends IModeAny{
    
    public static class EServerObjectNotFound extends RuntimeException{
        private static final long serialVersionUID = -6849318391905325406L;
        private String m_entityName;
        public EServerObjectNotFound(String entityName){
            super(entityName);
            this.m_entityName =entityName;
        }
        public String getEntityName() {
            return m_entityName;
        }    
    }
    
    /**
     * @param <T> Entity type
     * @param entityName Entity name
     * @return entity
     * @throws EServerObjectNotFound if entity not found
     */
    public <T extends IEntity> T getEntity(String entityName) throws EServerObjectNotFound;
    
    /**
     * @param <T> Entity type
     * @param entityName Entity name
     * @return entity or null if entity not found
     */
    public <T extends IEntity> T quieryEntity(String entityName);    
   
    /**
     * @param <T> Entity class
     * @param cls Class of Entity
     * @return List of registered entities inherited from cls
     */
    public <T extends IEntity> List<T> getEntities(Class<T> cls);

}
