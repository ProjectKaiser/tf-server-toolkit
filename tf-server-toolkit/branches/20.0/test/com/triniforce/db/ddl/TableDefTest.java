/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */

package com.triniforce.db.ddl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.EInvalidModificationSequence;
import com.triniforce.db.ddl.TableDef.EUnknownHistoryRequest;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

/**
 * Testing DBTable methods
 */
public class TableDefTest extends TestCase {
    private TableDef m_dbo;
    
    ArrayList<TableUpdateOperation> m_ops;
    
    public TableDefTest() {
        super();
        m_ops = new ArrayList<TableUpdateOperation>();
        m_ops.add(new AddColumnOperation(FieldDef.createScalarField("f1", ColumnType.INT,true)));
        m_ops.add(new AddColumnOperation(FieldDef.createScalarField("f2", ColumnType.INT,false)));
        m_ops.add(new AddColumnOperation(FieldDef.createScalarField("f3", ColumnType.INT,true)));
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        m_dbo = new TableDef("table1");
        super.setUp();
    }
       
    /**
     * Test object constructor
     */
    public void testCreation(){
        assertEquals(0, m_dbo.getVersion());		
        assertEquals("table1", m_dbo.getEntityName());
    }
    
    public void testDBTable(){
        {   //test max name length                
            String dbName = "Test.";
            for (int i = 0; i < 6; i++) {
                dbName = dbName+dbName;
            }
            try{
                m_dbo = new TableDef(dbName);
                fail();
            } catch(TableDef.EDBObjectException e){}
        }
        {   //no name
            TableDef tabDef = new TableDef();
            assertEquals("com.triniforce.db.ddl.TableDef", tabDef.getEntityName());
        }
    }
    
    /**
     * Test getting history, that nothing thave 
     */
    public void testGetNullHistory(){
        List<TableUpdateOperation> hist;
        try {
            hist = m_dbo.getHistory(1);
            assertNotNull(hist);
            assertEquals(0, hist.size());
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail("somthing wrong");
        }
    }
    
    /**
     * Test getting history from older version object
     * raise EVersionConflict exception 
     */
    public void testGetNotExistentHistory(){
        int version = m_dbo.getVersion();
        try{			
            m_dbo.getHistory(version + 10);
            fail();
        }
        catch(EUnknownHistoryRequest e){
            assertEquals(m_dbo.getEntityName(), e.m_dboName);
            assertEquals(version, e.m_dboVer);
            assertEquals(version+10, e.m_dboReqVer);			
        }
    }
    
    /**
     * Test add modification to object
     * and test history, he must store our modification 
     * @throws EDBObjectException 
     */
    public void testAddModification() throws EDBObjectException{
        int nextVersion = m_dbo.getVersion() + 1;        
        m_dbo.addModification(nextVersion, m_ops.get(0));    
        List<TableUpdateOperation> hist = m_dbo.getHistory(nextVersion);
        assertEquals(nextVersion, m_dbo.getVersion());
        assertTrue(hist.size() == 1);
        assertTrue(hist.contains(m_ops.get(0)));
    }	
    
    /**
     * Test add non sequence modification
     * object raise exception when sequence is 1,2,3,?5
     * @throws EDBObjectException 
     */
    public void testAddIncompatibeModification() throws EDBObjectException{
        {//wrong version
            int version = m_dbo.getVersion();
            try{
                m_dbo.addModification(version+2, m_ops.get(0));
                fail();
            }
            catch(EInvalidModificationSequence e){
                assertEquals(version, m_dbo.getVersion());
                assertEquals(m_dbo.getEntityName(), e.m_dboName);
            }
        }
    }
    
    /**
     * History must be ejected in addition sequence
     * first in first out  
     * @throws EDBObjectException 
     */
    public void testHistoryIterating() throws EDBObjectException
    {
        int v = m_dbo.getVersion();		//Prepare condition
        for (int i = 0; i < m_ops.size(); i++) {
            m_dbo.addModification(v+i+1, m_ops.get(i));
        }
        
        List<TableUpdateOperation> hist = m_dbo.getHistory(1);
        
        // Check for sequence in all history
        assertEquals(v+m_ops.size(), hist.size());
        int vi=0;
        for (Iterator<TableUpdateOperation> iter = hist.iterator(); iter.hasNext();) {
            TableUpdateOperation upd = iter.next();
            assertEquals(m_ops.get(vi), upd);
            vi ++;
        }
    }
    
    public void testAddModificationHelpers() throws EDBObjectException{
        m_dbo.addScalarField(1, "scalar_field1", FieldDef.ColumnType.FLOAT, true, Float.valueOf(43.2f));
        m_dbo.addStringField(2, "string_field1", FieldDef.ColumnType.CHAR, 64, true, "default string");
        m_dbo.addDecimalField(3, "decimal_field1", 12, 3, true, BigDecimal.valueOf(1413242,3));        
        m_dbo.addPrimaryKey(4, "pk_1", new String[]{"string_field1"});
        m_dbo.addForeignKey(5, "fk_1", new String[]{"string_field1"}, "parent_tab", "parent_key", true);
        m_dbo.addIndex(6, "index_1", new String[]{"string_field1"}, true, true);
        m_dbo.addScalarField(7, "scalar_field2", FieldDef.ColumnType.FLOAT, true, Float.valueOf(43.2f));
        m_dbo.addIndex(8, "index_2", new String[]{"string_field1"}, true, true);
        m_dbo.deleteField(9, "scalar_field2");
        m_dbo.deleteIndex(10, "index_2");
        m_dbo.addIndex(11, "index_3", new String[]{"string_field1"}, true, true);
        m_dbo.deleteIndex(12, "index_3");
        
        assertEquals(12, m_dbo.getVersion());
        assertEquals(3, m_dbo.getFields().size());
        assertEquals(3, m_dbo.getIndices().size());
        
        DeleteIndexOperation op = (DeleteIndexOperation) m_dbo.getHistory(12).get(0);
        assertEquals(IndexDef.TYPE.INDEX, op.getType());
        assertEquals(true, op.isUniqueIndex());
        
//        m_dbo.addPrimaryKey(13, "pk_001", new String[]{"string_field1"});
        m_dbo.deleteIndex(13, "fk_1");
        
        op = (DeleteIndexOperation) m_dbo.getHistory(13).get(0);
        assertEquals(IndexDef.TYPE.FOREIGN_KEY, op.getType());
        assertEquals(false, op.isUniqueIndex());
    }
    
    public void testClone() throws CloneNotSupportedException{
        m_dbo.addScalarField(1, "scalar_field1", FieldDef.ColumnType.FLOAT, true, Float.valueOf(43.2f));
        m_dbo.addStringField(2, "string_field1", FieldDef.ColumnType.CHAR, 64, true, "default string");
        m_dbo.addDecimalField(3, "decimal_field1", 12, 3, true, BigDecimal.valueOf(1413242,3));        
        m_dbo.addPrimaryKey(4, "pk_1", new String[]{"string_field1"});
        m_dbo.addForeignKey(5, "fk_1", new String[]{"string_field1"}, "parent_tab", "parent_key", true);
        m_dbo.addIndex(6, "index_1", new String[]{"string_field1"}, true, true);

        TableDef cloned = (TableDef) m_dbo.clone();
        
        assertEquals(m_dbo.getVersion(), cloned.getVersion());
        assertEquals(m_dbo.getEntityName(), cloned.getEntityName());
        assertNotSame(m_dbo, cloned);
        assertEquals(m_dbo.getFields().getElement(1), cloned.getFields().getElement(1));
        assertEquals(m_dbo.getIndices().size(), cloned.getIndices().size());
    }
}
