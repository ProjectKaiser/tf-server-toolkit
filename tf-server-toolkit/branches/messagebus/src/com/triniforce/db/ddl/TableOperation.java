/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import com.triniforce.db.ddl.TableDef.EMetadataException;

public abstract class TableOperation {

    /**
     * @return name of operation
     */
    public abstract String getName();
    
    /**
     * @return Rollback operation
     */
    public abstract TableOperation getReverseOperation();
    
    /**
     * @param table - operated table
     * @throws EMetadataException - metadata error
     */
    public void apply(TableDef table) throws EMetadataException{
        throw new EMetadataException(table.getEntityName(), getName()); //$NON-NLS-1$
    }
    
    public abstract int getVersionIncrease();
    
}
