/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import junit.framework.TestCase;

import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

public class AlterColumnOperationTest extends TestCase{

    private AlterColumnOperation m_op;
    
    @Override
    protected void setUp() throws Exception {
        m_op = new AlterColumnOperation(
                FieldDef.createScalarField("col1", ColumnType.FLOAT, true), 
                FieldDef.createScalarField("col1", ColumnType.INT, false));
        
        super.setUp();
    }
    
    public void testAlterColumnOperation(){
        {
            assertEquals("col1", m_op.getName());
            assertTrue(m_op.bSetType());
            assertTrue(m_op.bSetNotNullFlag());
            assertEquals(FieldDef.createScalarField("col1", ColumnType.INT, false), m_op.getNewField());
            assertEquals(1, m_op.getVersionIncrease());
            try{
//                m_op.apply(null);
            }catch(EMetadataException e){}
        }
        {
            try{
                new AlterColumnOperation(
                    FieldDef.createDecimalField("col1", 15, 12, true, "0.0"), 
                    FieldDef.createScalarField("col3", ColumnType.INT, true));
                fail();
            } catch(TableDef.EInvalidDefinitionArgument e) {}          
        }
    }
}
