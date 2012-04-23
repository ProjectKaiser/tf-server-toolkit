/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.ddl;

import java.sql.Types;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.TFTestCase;

public class FieldDefTest extends TFTestCase {
    
    public void testGetSqlType(){
        assertEquals( Types.BIGINT, FieldDef.sqlType(ColumnType.LONG));        
    }

}
