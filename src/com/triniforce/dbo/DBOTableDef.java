/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.dbo;

import com.triniforce.db.ddl.TableDef;

public class DBOTableDef extends TableDef implements IDBObject {

    public IDBObject[] getDependiencies() {
        return new IDBObject[]{};
    }

    public IDBObject[] synthDBObjects() {
        return new IDBObject[]{};
    }

    public Class getActualizerClass() {
        return ExDBOATables.class;
    }

    public Object getKey() {
        return getEntityName();
    }

}
