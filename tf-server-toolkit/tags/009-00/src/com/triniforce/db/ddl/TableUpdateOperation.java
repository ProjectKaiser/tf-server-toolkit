/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */

package com.triniforce.db.ddl;

import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.IElementDef;

public abstract class  TableUpdateOperation<T extends IElementDef> extends TableOperation{
    
    /**
     * @param table - operated table
     * @throws EMetadataException - metadata error
     */
    public abstract void apply(TableDef table) throws EMetadataException;
    
    @Override
    public int getVersionIncrease() {
    	return 1;
    }
        
}
