/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.filesystem.tables;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.triniforce.db.dml.Table;
import com.triniforce.db.dml.TableAdapter;
import com.triniforce.db.dml.Table.Row;
import com.triniforce.db.test.DBTestCase;
import com.triniforce.server.plugins.kernel.tables.EntityJournal;
import com.triniforce.server.srvapi.UpgradeProcedure;

public class EntityJournalTest extends DBTestCase {

    private EntityJournal<UpgradeProcedure> m_def;
    private String m_dbName;        
    
    private class TestProc1 extends UpgradeProcedure{
        public TestProc1() {
            super("TestProc1.");
        }
    }
    private class TestProc2 extends UpgradeProcedure{
        public TestProc2() {
            super("TestProc2.");
        }
    }
    private class TestProc3 extends UpgradeProcedure{
        public TestProc3() {
            super("TestProc3.");
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        m_def = new EntityJournal<UpgradeProcedure>("com.triniforce.server.filesystem.tables.EntityJournalTest", getDbType());
        m_dbName = createTableIfNeeded(m_def);
        
        Table tab = new Table();
        tab.addColumn(m_def.getFields().getElement(0));
        Row row = tab.newRow();
        row.setField(EntityJournal.ENTITY_NAME_FIELD, "com.triniforce.server.filesystem.tables.UpgradeProceduresTest.originalProc1");
        new TableAdapter().flush(getConnection(), tab, m_def, m_dbName);
    }
    
    public void testAdd() throws SQLException, Exception{
        m_def.add(getConnection(), m_dbName, Arrays.asList(new UpgradeProcedure[]{new TestProc2()}));        
        assertEquals(Collections.emptyList(), m_def.exclude(getConnection(), m_dbName, Arrays.asList(new UpgradeProcedure[]{new TestProc2()})));
    }   
    
    public void testExclude() throws SQLException, Exception{
        m_def.add(getConnection(), m_dbName, Arrays.asList(new UpgradeProcedure[]{new TestProc2()}));
        
        List<UpgradeProcedure> res = m_def.exclude(getConnection(), m_dbName, Arrays.asList(new UpgradeProcedure[]{new TestProc1(), new TestProc2(), new TestProc3()}));        
        assertEquals(2, res.size());
        
        m_def.add(getConnection(), m_dbName, res);
        assertEquals(Collections.emptyList(), m_def.exclude(getConnection(), m_dbName, Arrays.asList(new UpgradeProcedure[]{new TestProc2()})));
        
    }
    
    public void testGetActual() throws SQLException, Exception{
        m_def.add(getConnection(), m_dbName, Arrays.asList(new UpgradeProcedure[]{new TestProc2()}));
        Set<String> actual = m_def.getActual(getConnection(), m_dbName);
    	assertTrue(actual.contains(TestProc2.class.getName()));
    }

}
