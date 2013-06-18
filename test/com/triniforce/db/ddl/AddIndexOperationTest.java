/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.util.Arrays;

import com.triniforce.db.ddl.TableDef.IndexDef;

import junit.framework.TestCase;

public class AddIndexOperationTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testAddIndexOperation(){
        new AddIndexOperation(IndexDef.createIndex("index_name", Arrays.asList("index_column"), true, false, false));        
    }

}
