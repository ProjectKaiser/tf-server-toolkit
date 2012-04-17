/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

import junit.framework.TestCase;

public class DeleteColumnOperationTest extends TestCase {

    private TableDef m_dbTable;

    /*
     * Test method for 'com.triniforce.db.ddl.DeleteColumnOperation.DeleteColumnOperation(String)'
     */
    public void testDeleteColumnOperation() {
        DeleteColumnOperation op = new DeleteColumnOperation("col1");
        assertEquals("col1", op.getName());
    } 

    /*
     * Test method for 'com.triniforce.db.ddl.DeleteColumnOperation.apply(DBTable)'
     */
    public void testApply() throws EMetadataException {
        {
            DeleteColumnOperation op = new DeleteColumnOperation("col1");       
            op.apply(m_dbTable);        
            assertTrue("Fields must be empty", m_dbTable.getFields().size()==0);
        }
        {
            DeleteColumnOperation op = new DeleteColumnOperation("unknown_column");
            try{
                op.apply(m_dbTable);
                fail("no field exception must raised");
            } catch(EMetadataException e){
                assertEquals(m_dbTable.getEntityName(), e.m_dboName);
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        m_dbTable = new TableDef("table");
        m_dbTable.getFields().addElement(FieldDef.createScalarField("col1", ColumnType.INT, true));
        super.setUp();
    }
    
    
    public void testGetReverseOperation(){
        DeleteColumnOperation op = new DeleteColumnOperation(FieldDef.createScalarField("f1", ColumnType.INT, true));
        TableUpdateOperation revOp = op.getReverseOperation();
        assertTrue("Must be AddColumn", revOp instanceof AddColumnOperation);
        AddColumnOperation addOp = (AddColumnOperation)revOp;
        assertEquals(FieldDef.createScalarField("f1", ColumnType.INT, true), addOp.getField());
    }


}
