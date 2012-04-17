/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import junit.framework.TestCase;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.ENameRedefinition;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

public class AddColumnOperationTest extends TestCase {


    private FieldDef m_f1;

    @Override
    protected void setUp() throws Exception {
        m_f1 = FieldDef.createScalarField("field1", ColumnType.INT, true);
        super.setUp();
    }
    
    /*
     * Test method for 'com.triniforce.db.ddl.AddColumnOperation.AddColumnOperation(FieldDef)'
     */
    public void testAddColumnOperation(){
        AddColumnOperation op = new AddColumnOperation(m_f1);
        assertEquals(m_f1, op.getField());
        assertEquals(op.getName(), m_f1.m_name);
    }

    /*
     * Test method for 'apply(DBTable)'
     */
    public void testApply() throws EDBObjectException{
        {
            AddColumnOperation op = new AddColumnOperation(m_f1);     
            TableDef dbo = new TableDef("table");
            op.apply(dbo);
            assertNotNull(dbo.getFields());
            assertEquals(1, dbo.getFields().size());
            assertSame(m_f1, dbo.getFields().getElement(0));
        }
        {
            AddColumnOperation op = new AddColumnOperation(m_f1);     
            TableDef dbo = new TableDef("table");          
            op.apply(dbo);
            try{
                op.apply(dbo);
            } catch(ENameRedefinition e){
                assertEquals("table", e.m_dboName);                
                assertEquals(m_f1.m_name, e.getObject());                                
            }
        }
    }

    public void testgetReverseOperation(){
        AddColumnOperation op = new AddColumnOperation(m_f1);
        TableUpdateOperation revOp = op.getReverseOperation();
        assertTrue("Must be DelColumn", revOp instanceof DeleteColumnOperation);
        DeleteColumnOperation delOp = (DeleteColumnOperation)revOp;
        assertEquals(m_f1, delOp.getDeletedField());
        assertEquals(m_f1.m_name, delOp.getName());
    }
    
}

