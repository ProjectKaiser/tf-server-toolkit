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
import com.triniforce.db.ddl.TableDef.EInvalidModificationSequence;
import com.triniforce.db.ddl.TableDef.ENameRedefinition;
import com.triniforce.db.ddl.TableDef.EUnknownReference;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

public class AddForeignKeyOperationTest extends TestCase {

    /*
     * Test method for 'com.triniforce.db.ddl.AddForeignKeyOperation.AddForeignKeyOperation(DBTable, String, List<String>, DBTable, List<String>)'
     */
    public void testAddForeignKeyOperation() throws EInvalidModificationSequence{
        List<String> idx1 = Arrays.asList("column1", "column2");
        AddForeignKeyOperation addFK = new AddForeignKeyOperation("fk1", idx1, "tab2", "pk1");
        assertEquals(addFK.getIndex(), IndexDef.foreignKey("fk1", idx1, "tab2", "pk1"));
        
        assertTrue(addFK.isCreateFK());
    }

    /*
     * Test method for 'com.triniforce.db.ddl.AddForeignKeyOperation.apply()'
     */
    public void testApply() throws EDBObjectException{
        TableDef dbo = new TableDef("table");
        dbo.addModification(1, new AddColumnOperation(FieldDef.createScalarField("column1", ColumnType.INT, true)));
        {
            AddForeignKeyOperation op = new AddForeignKeyOperation("idx1", Arrays.asList("column1"), "tab2", "unknown");     
            op.apply(dbo);
            assertNotNull(dbo.getIndices());
            assertEquals(1, dbo.getIndices().size());
            assertEquals("idx1", dbo.getIndices().getElement(0).m_name);
        }
        {
            AddForeignKeyOperation op = new AddForeignKeyOperation("idx2", Arrays.asList("unknown"), "tab2", "unknown");     
            try{
                op.apply(dbo);
                fail();
            } catch(EUnknownReference e){
                assertEquals("table", e.m_dboName);                
                assertEquals("idx2", e.m_opName);                                
                assertEquals("unknown", e.m_refName);                                
            }
        }
        {
            AddForeignKeyOperation op = new AddForeignKeyOperation("idx3", Arrays.asList("column1"), "tab2", "unknown");     
            op.apply(dbo);
            try{
                op.apply(dbo);
                fail();
            } catch(ENameRedefinition e){
                assertEquals("table", e.m_dboName);                
                assertEquals("idx3", e.getObject());                                
            }
        } 
    }
    
    public void testSetRefColumns(){
        AddForeignKeyOperation op = new AddForeignKeyOperation("fk1", Arrays.asList("col1"), "tab2", "pk1");
        op.setRefColumns(Arrays.asList("refCol1", "refCol2"));
        List<String> cols = op.getRefColumns();
        assertEquals(2, cols.size());
        assertTrue(Arrays.asList("refCol1", "refCol2").containsAll(cols));
    }

}
