/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */


package com.triniforce.db.dml;

import java.sql.SQLException;

import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.dml.Table.Row;
import com.triniforce.db.dml.Table.UnknownFieldException;

public class TableTest extends DMLTestCase {
    

    private Table m_t;

    protected void setUp() throws Exception {
        super.setUp();        
        m_t = new Table();
        m_t.addColumn(getTabDef().getFields().getElement(0));
        m_t.addColumn(getTabDef().getFields().getElement(1));
        m_t.addColumn(getTabDef().getFields().getElement(2));
        m_t.populate(getRS());
    }

    public void testTable() {
        Table t = new Table();
        assertEquals(0, t.getSize());
    }

    public void testAddColumn() {
        int fnum = m_t.getFieldDefs().size();
        
        FieldDef f = getTabDef().getFields().getElement(3);
        m_t.addColumn(f);
        assertEquals(f.getDefaultValue(), m_t.getRow(2).getField(3));
        
        assertEquals(f, m_t.getFieldDefs().get(fnum));
    }

    public void testPopulate() throws SQLException {
        assertEquals(getRS().size(), m_t.getSize());
        getRS().beforeFirst();
        int i=0;
        while(getRS().next()){
            Row r = m_t.getRow(i);
            assertEquals(Row.State.INTACT, r.getState());
            assertEquals(getRS().getInt("ID"), r.getField("ID"));
            assertEquals(getRS().getString("Name"), r.getField("Name"));
            assertEquals(getRS().getString("Phone"), r.getField("Phone"));
            i++;
        }
    }

    public void testGetSize() {
        assertEquals(3, m_t.getSize());
//        m_t.getRow(2).delete();
//        assertEquals(2, m_t.getSize());
        m_t.newRow();
        assertEquals(4, m_t.getSize());
        
    }

    public void testGetRow() throws SQLException {
        Row r = m_t.getRow(1);
        getRS().absolute(2);
        assertEquals(Row.State.INTACT, r.getState());
        assertEquals(getRS().getInt("ID"), r.getField("ID"));
        assertEquals(getRS().getString("Name"), r.getField("Name"));
        assertEquals(getRS().getString("Phone"), r.getField("Phone"));
    }

    public void testNewRow() {
        Row r = m_t.newRow();
        assertEquals(Row.State.INSERTED, r.getState());
        assertEquals(getTabDef().getFields().getElement(0).getDefaultValue(), r.getField("ID"));
        assertEquals(getTabDef().getFields().getElement(1).getDefaultValue(), r.getField("Name"));
        assertEquals(getTabDef().getFields().getElement(2).getDefaultValue(), r.getField("Phone"));        
    }
    
    public void testRowSetField(){
        {
            Row r = m_t.getRow(0);
            r.setField("Phone", "777-00-00");
            assertEquals(r.getField("phOne"), "777-00-00");
            assertEquals(Row.State.UPDATED, r.getState());
        }
        {
            Row r = m_t.getRow(0);
            try{
                r.setField("UnknownField", "777-00-00");
                fail();
            } catch(UnknownFieldException e){}
        }        
    }
    
    public void testRowGetField() throws SQLException{
        getRS().absolute(1);
        assertEquals(getRS().getInt(1), m_t.getRow(0).getField(0));
    }
    
    public void testRowDelete(){
        {
            m_t.getRow(1).delete();
            assertEquals(Row.State.DELETED, m_t.getRow(1).getState());
        }
        {
            Row r = m_t.newRow();
            r.delete();
            assertEquals(Row.State.CANCELED, m_t.getRow(3).getState());
        }
    }
    
    public void testRowGetOriginal() throws SQLException{
        {
            Row r = m_t.getRow(1);
            r.setField("Phone", "526-78-87");
            getRS().absolute(2);
            assertEquals(getRS().getString("Phone"), r.getOriginalField("Phone"));
            assertEquals("526-78-87", r.getField("Phone"));
            assertEquals("526-78-87", r.getField("Phone"));
        }
        {
            Row r = m_t.getRow(0);
            r.setField("Phone", "526-78-87");
            r.setField("ID", 632);
            r.setField("Name", "Sole Campbell");
            getRS().absolute(1);
            assertEquals(getRS().getString("Name"), r.getOriginalField("Name"));
            assertEquals(getRS().getString("Phone"), r.getOriginalField("Phone"));
            assertEquals(632, r.getField("ID"));
            assertEquals("Sole Campbell", r.getField("Name"));
            assertEquals("526-78-87", r.getField("Phone"));
        }
        {
            Row r = m_t.getRow(2);
            r.setField("ID", 633);
            r.setField("Phone", "526-78-87");
            r.setField("Name", "Sole Campbell");
            getRS().absolute(3);
            assertEquals(getRS().getString("Name"), r.getOriginalField("Name"));
            assertEquals(getRS().getString("Phone"), r.getOriginalField("Phone"));
            assertEquals(633, r.getField("ID"));
            assertEquals("Sole Campbell", r.getField("Name"));
            assertEquals("526-78-87", r.getField("Phone"));
        }
    }
    
    public void testAcceptChanges(){
        m_t.newRow().setField(0, 56);
        m_t.getRow(0).setField(1, "Dobra Naus");
        m_t.getRow(2).delete();
        m_t.newRow().setField(0, 58);
        
        m_t.acceptChanges();
        
        assertEquals(4, m_t.getSize());
        assertEquals("Dobra Naus", m_t.getRow(0).getOriginalField(1));
        assertEquals("Dobra Naus", m_t.getRow(0).getField(1));
        assertEquals(56, m_t.getRow(2).getField(0));        
        assertEquals(58, m_t.getRow(3).getField(0));
    }

    public void testContainField(){
        assertTrue(m_t.containField("name"));
        assertFalse(m_t.containField("description"));
        
    }

    public void testRowAccept(){
        {//insert
            int sz = m_t.getSize();
            Row row = m_t.newRow();
            row.setField(0, 56);
            row.accept();
            assertEquals(Row.State.INTACT, row.getState());
            assertEquals(56, row.getField(0));
            row = m_t.getRow(sz);
            assertEquals(Row.State.INTACT, row.getState());
            assertEquals(56, row.getField(0));
        }
        {   //update
            Row row = m_t.getRow(0);
            row.setField(1, "Dobra Naus");
            row.accept();
            assertEquals(Row.State.INTACT, row.getState());
            assertEquals("Dobra Naus", row.getOriginalField(1));            
        }
        {
            Row row = m_t.getRow(2);
            row.delete();
            row.accept();
            try{
                row.setField(0, 0);
                fail();
            } catch(RuntimeException e){}
        }
    }
//    
//    public void testCreateIndex(){
//    	UniqueIndex res = m_t.createIndex("name");
//    	assertNotNull(res);
//    	
//    	assertEquals(1, res.get("Serg Bake").getField(0));
//    	assertEquals(2, res.get("James Bond").getField(0));
//    	
//    	try{
//    		m_t.createIndex("unk_column");
//    		fail();
//    	} catch(UnknownFieldException e){}
//    	
//    	// add row, index refreshing
//    	Row r = m_t.newRow();
//    	r.setField("name", "Joseph T");
//    	r.setField("id", 57);
//    	
//    	assertEquals(57, res.get("Joseph T").getField(0));
//    }
    
}
