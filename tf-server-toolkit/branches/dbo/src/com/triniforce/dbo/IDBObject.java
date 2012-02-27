/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.dbo;

public interface IDBObject {

    /**
     * @return empty array if none
     */
    IDBObject[] getDependiencies();

    /**
     * @return empty array if none
     */
    IDBObject[] synthDBObjects();
    
    Class getActualizerClass();
    
    /**
     * @return 
     */
    Object getKey();
}
