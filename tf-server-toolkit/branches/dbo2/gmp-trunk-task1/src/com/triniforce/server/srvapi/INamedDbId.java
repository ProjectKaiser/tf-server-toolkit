/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.srvapi;

public interface INamedDbId extends IModeAny{
    
    @SuppressWarnings("serial")
    public static class ENotFound extends RuntimeException{
        public ENotFound(String name){
            super(name); 
        }
        public ENotFound(long id){
            super(Long.toString(id)); 
        }        
    }
   
    
    /**
     * Returns null if name not found
     */
    Long queryId(String name);
    
    long getId(String name) throws ENotFound;
    
    /**
     * Id must be created and commited in separate transaction. 
     * <p>If name already exists its id is returned
     *  
     */
    long createId(String name);
    
    /**
     * id must be dropped and commited in separate transaction
     */
    void dropId(long id);
    
    
    String getName(long id)throws ENotFound;
    
}
