/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import java.sql.SQLException;

import com.triniforce.server.srvapi.ISOQuery.EServerObjectNotFound;

/**
 * Running mode info about SO
 *
 */
public interface ISODbInfo extends IModeRunning {
    /**
     * @param entityName Table entity name
     * @return Name of table in database 
     * @throws EServerObjectNotFound if object not found or it is not a table
     * @throws SQLException 
     * 
     */
    public String getTableDbName( String entityName) throws EServerObjectNotFound ;
    
    /**
     * @param entityName name like 'com.triniforce.server.myprop';
     * @return DbInfo for given property
     * @throws EServerObjectNotFound if object not found or it is not a property 
     */
    //public PropDbInfo getPropDbInfo(String entityName) throws  EServerObjectNotFound ;
    
     /** 
     * 
     * Returns id of entity, entity name is considered as class name of cls 
     * @param cls
     * @return
     * @throws EServerObjectNotFound
     */
    //public int getEntityId(Class cls) throws  EServerObjectNotFound; 

     /** 
     * 
     * Returns id of entity ( e.g. File or Property ) 
     * @param entityName
     * @return
     * @throws EServerObjectNotFound
     */
    //public int getEntityId(String entityName) throws  EServerObjectNotFound;
    
    /**
     * Return name of entity ( e.g. File or Property )
     * @param id
     * @return
     * @throws EServerObjectNotFound
     */
    //public String getEntityName(int id) throws EServerObjectNotFound;
}
