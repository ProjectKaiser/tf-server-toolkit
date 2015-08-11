/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.utils;

public interface IEntity { 
 
    public static final int MAX_ENTITY_NAME = 255;    
    
    /**
     * @return name like com.triniforce.server.filesystem.File 
     * 
     */
    public String getEntityName();
   
}
