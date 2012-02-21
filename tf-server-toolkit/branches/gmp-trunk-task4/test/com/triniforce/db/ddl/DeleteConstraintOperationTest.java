/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.util.Arrays;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

import junit.framework.TestCase;

public class DeleteConstraintOperationTest extends TestCase {

    private TableDef m_dbTable;

    protected void setUp() throws Exception {
        m_dbTable = new TableDef("table");
        m_dbTable.getFields().addElement(FieldDef.createScalarField("col1", ColumnType.INT, true));
        m_dbTable.getIndices().addElement(IndexDef.primaryKey("pk1", Arrays.asList("col1")));
        super.setUp();    
    }
    
    public void testDeleteConstraintOperation(){
        DeleteIndexOperation op=new DeleteIndexOperation("pk1", IndexDef.TYPE.PRIMARY_KEY, false);
        assertEquals("pk1", op.getName());
        assertEquals(IndexDef.TYPE.PRIMARY_KEY, op.getType());
    }
    
    public void testApply() throws EMetadataException{
        {
            DeleteIndexOperation op = new DeleteIndexOperation("unknown_constraint", IndexDef.TYPE.INDEX, false);
            try{
                op.apply(m_dbTable);
                fail();
            } catch(EMetadataException e){}
        }
        {
            DeleteIndexOperation op = new DeleteIndexOperation("pk1", IndexDef.TYPE.PRIMARY_KEY, false);
            op.apply(m_dbTable);
            assertTrue(m_dbTable.getIndices().size()==0);
        }
    }
    
    public void testGetReverseOperation() throws EDBObjectException{
    	
        {
            TableDef tab = new TableDef("tab");
            tab.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab.addModification(2, new AddIndexOperation(IndexDef.createIndex("name", Arrays.asList("col1"), true, false)));
            tab.addModification(3, new DeleteIndexOperation("name", IndexDef.TYPE.INDEX, false));
            TableOperation revOp = tab.getHistory(3).get(0).getReverseOperation();
            assertTrue("Must be AddIndex", revOp.getClass().equals(AddIndexOperation.class));
            AddIndexOperation addOp = (AddIndexOperation)revOp;
            assertEquals(IndexDef.createIndex("name", Arrays.asList("col1"), true, false), addOp.getIndex());            
        }
    }
}
