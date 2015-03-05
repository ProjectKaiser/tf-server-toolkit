/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 
package com.triniforce.db.ddl;

import java.util.List;

import com.triniforce.db.ddl.TableDef.IndexDef;

/**
 * Adding primary key to database table
 */
public class AddPrimaryKeyOperation extends AddIndexOperation{


    /**
     * @param name
     *          constraint (index) name
     * @param columns
     *          index columns
     */
    public AddPrimaryKeyOperation(String name, List<String> columns){
        super(IndexDef.primaryKey(name, columns));
    }
}
