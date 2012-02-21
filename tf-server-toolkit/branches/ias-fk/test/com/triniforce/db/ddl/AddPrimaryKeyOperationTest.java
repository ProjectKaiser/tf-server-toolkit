/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

public class AddPrimaryKeyOperationTest extends TestCase {

    /*
     * Test method for 'com.triniforce.db.ddl.AddPrimaryKeyOperation.AddPrimaryKeyOperation(DBTable, String, List<String>)'
     */
    public void testAddPrimaryKeyOperation(){
        List<String> index = Arrays.asList("column1", "column2");
        AddPrimaryKeyOperation addPK = new AddPrimaryKeyOperation("primary_key1", index);
        assertEquals(addPK.getIndex(), IndexDef.primaryKey("primary_key1", index));
    }
    
    /*
     * Test method for 'com.triniforce.db.ddl.AddPrimaryKeyOperation.apply()'
     */
    public void testApply() throws EDBObjectException{
        TableDef dbo = new TableDef("table");
        dbo.addModification(1, new AddColumnOperation(FieldDef.createScalarField("column1", ColumnType.INT, true)));
        {
            AddPrimaryKeyOperation op = new AddPrimaryKeyOperation("idx1", Arrays.asList("column1"));     
            op.apply(dbo);
            assertNotNull(dbo.getIndices());
            assertEquals(1, dbo.getIndices().size());
            assertEquals("idx1", dbo.getIndices().getElement(0).m_name);
        }

    }

}
