/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import com.triniforce.db.ddl.TableDef;

/**
 * ServerCorePlugin uses this interface to register Server objects
 *
 */
public interface ISORegistration extends IModeRegistration{    

  
    /**
     * Registers single stand-alone table definition which is not a part of
     * any file
     */
    public void registerTableDef(TableDef tableDef);
    
    /**
     * Registers file. All file tables will be registered automatically
     * , it is not needed to register them separately
     */    
    //public void registerFile(File fileDef);
    
    /**
     * Registers property definition. Implementor registers table for property
     * , if needed.
     * @throws EDBObjectException 
     */
    //public void registerProperty(PropDef propDef) throws EDBObjectException;
    
    /**
     * Registers upgrade procedure.
     */
    public void registerUpgradeProcedure(UpgradeProcedure updProc);
    
    /**
     * Registers data preparation procedure.
     */
    public void registerDataPreparationProcedure(DataPreparationProcedure proc);    
    
    /**
     * Registers select function definition
     */
    //public void registerSelectFunction(SelectFunctionDef sfDef);    
    
    /**
     * Registers select filter definition
     */
    //public void registerSelectFilter(SelectFilterDef sfDef);
    
    /**
     * Registers blob definition
     */
    //public void registerBlob(BLOB sfDef);    

    
}
