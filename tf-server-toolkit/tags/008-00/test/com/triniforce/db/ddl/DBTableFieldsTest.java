/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;


import junit.framework.TestCase;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

@SuppressWarnings("unchecked") //no generics, otherwise is not compiled under linux
public class DBTableFieldsTest extends TestCase {

    private TableDef m_dbo;
    private com.triniforce.db.ddl.TableDef.Fields m_fields;//no generics, otherwise is not compiled under linux
    FieldDef m_f1;
    FieldDef m_f2;

    @Override
    protected void setUp() throws Exception {
        m_dbo = new TableDef("table");
        m_fields = m_dbo.getFields();
        m_f1 = FieldDef.createScalarField("f1", ColumnType.INT, false);
        m_f2 = FieldDef.createStringField("f2", ColumnType.CHAR, 16, true, "''");
        assertNotNull(m_fields);
        super.setUp();
    }
    
    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.Fields.size()'
     */
    public void testSize() {
        assertEquals(0, m_fields.size());
    }

    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.Fields.get(int)'
     */
    public void testGetInt() throws EMetadataException {
        m_fields.addElement(m_f1);
        assertEquals(1, m_fields.size());        
        assertSame(m_f1, m_fields.getElement(0));
    }

    /*
     * Test method for 'com.triniforce.db.ddl.DBTable.Fields.add(int, FieldDef)'
     */
    public void testAddNode() throws EMetadataException {
        {
            m_fields.addElement(m_f1);
            m_fields.addElement(m_f2);
            assertEquals(2, m_fields.size());                
            assertSame(m_f1, m_fields.getElement(0));
            assertSame(m_f2, m_fields.getElement(1));
        }
        try{
            m_fields.addElement(m_f1);
            fail();
        } catch(EMetadataException e){} 
    }


    public void testGetNodeIdx() throws EMetadataException{
        m_fields.addElement(m_f1);
        m_fields.addElement(m_f2);
        assertEquals(m_f2, m_fields.findElement(m_f2.getName()).getElement());
    }
    
    public void testGetNodeVersionInt() throws EDBObjectException{
        m_dbo.addModification(1, new AddColumnOperation(m_f1));
        m_dbo.addModification(2, new AddColumnOperation(m_f2));
        int v = m_fields.getElementVersion(1);
        assertEquals(2, v);
    }
}

