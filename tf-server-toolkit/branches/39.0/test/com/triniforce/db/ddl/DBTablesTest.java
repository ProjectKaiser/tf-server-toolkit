/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */

package com.triniforce.db.ddl;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.TableDef.ECycleReference;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.EInvalidDefinitionArgument;
import com.triniforce.db.ddl.TableDef.EUnknownReference;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.UpgradeRunner.IActualState;

/**
 * Test DBObjects operations
 *
 */
public class DBTablesTest extends TestCase {

    private DBTables m_db;
    HashMap<String, TableDef> m_plugin;
    HashMap<String, Integer> m_as;
    
    static class TestAS implements IActualState{
        
        private HashMap<String, Integer> as;
        private HashMap<String, String> dbNames = new HashMap<String, String>();

        public TestAS(HashMap<String, Integer> as) {
            this.as = as;
        }

        public void addTable(String dbName, String tabName, int v) {
        	dbNames.put(tabName, dbName);
        }

        public void changeVersion(String tabName, int dv) {
        }

        public String generateTabName(String appTabName) throws SQLException {
            return null;
        }

        public String getAppName(String dbName) throws SQLException {
            return null;
        }

        public String getDBName(String appName) {
            return dbNames.get(appName);
        }

        public int getVersion(String appName){
            Integer res = as.get(appName);
            return null == res ? 0 : res;
        }

        public HashMap<String, Integer> getVersionMap(Collection<String> tabs) throws SQLException {
            return null;
        }

        public void removeTable(String tabName) {
        }

        public boolean tableExists(String appTableName) throws SQLException {
            return false;
        }

		public void addIndexName(String appName, String dbName) {
		}

		public String generateIndexName(String appName) {
			fail();
			return null;
		}

		public String getIndexDbName(String appName) {
			fail();
			return null;
		}

		public void deleteIndexName( String appName) {
			fail();
		}

		public void flush(Connection conn) throws SQLException {
			fail();
		}

		public String queryIndexName(String dbTabName) {
			fail();
			return null;
		}
        
    }
	
    @Override
    protected void setUp() throws Exception {
        m_plugin = new HashMap<String, TableDef>();
        m_as = new HashMap<String, Integer>();
        m_db = new DBTables(new TestAS(m_as), m_plugin);
        
        super.setUp();
    }	
    
    public void testDBObjectsAccess(){
    }

    
    /**
     * Test command list
     * each dependent command must follow after parent object
     * @throws EDBObjectException 
     * @throws EInvalidDefinitionArgument 
     *  
     */
    public void testGenerateCommandList() throws EDBObjectException{
        {//Empty cycle
            List<DBOperation> cl = m_db.getCommandList();
            assertNotNull(cl);
            assertTrue(cl.isEmpty());
        }
        {//1 simple operation
            TableDef tab = new TableDef("table1");
            m_db.add(tab);
            TableUpdateOperation op = new AddColumnOperation(FieldDef.createScalarField("column1", ColumnType.INT, true));
            tab.addModification(1, op);
            List<DBOperation> cl = m_db.getCommandList();
            assertEquals(1, cl.size());
            assertEquals("table1", cl.get(0).getDBOName());
            assertTrue(cl.get(0).getOperation() instanceof CreateTableOperation);
        }
        {//1 simple operation, but in actual state - 0 op
            m_as.put("table1", 1);
            List<DBOperation> cl = m_db.getCommandList();
            assertTrue("operation in actual state", cl.isEmpty());            
        }
        {//3 operation, 1 in actual state, 2 in command list 
            TableDef tab = m_db.get("table1");
            AddColumnOperation op2 = new AddColumnOperation(FieldDef.createScalarField("column2", ColumnType.INT, false));
            tab.addModification(2, op2);
            AddPrimaryKeyOperation op3 = new AddPrimaryKeyOperation("pk1", Arrays.asList("column1"));
            tab.addModification(3, op3);
            List<DBOperation> cl = m_db.getCommandList();
            assertEquals(2, cl.size());
            assertEquals(op2, cl.get(0).getOperation());
            assertEquals(op3, cl.get(1).getOperation());
        }
        {//5 operation in command list, 
         // table2.AddColumn column1 before table1.AddFK fk1
            TableDef tab2 = new TableDef("table2");
            tab2.addModification(1, new AddColumnOperation(FieldDef.createScalarField("column0", ColumnType.INT, true)));
            tab2.addModification(2, new AddColumnOperation(FieldDef.createScalarField("column1", ColumnType.INT, true)));
            AddPrimaryKeyOperation tab2_pk1 =new AddPrimaryKeyOperation("pk1", Arrays.asList("column1")); 
            tab2.addModification(3, tab2_pk1);
            AddForeignKeyOperation tab1_fk1 = new AddForeignKeyOperation("fk1", Arrays.asList("column1"), "table2", "pk1");
            m_db.get("table1").addModification(4, tab1_fk1);
            m_db.add(tab2);
            m_as.put("table2", 1);
            List<DBOperation> cl = m_db.getCommandList();
            assertEquals(5, cl.size());
            int idx_fk = cl.indexOf(new DBOperation("table1", tab1_fk1));
            int idx_pk = cl.indexOf(new DBOperation("table2", tab2_pk1));
            assertTrue("Find tab1_fk1 failed",idx_fk >= 0);
            assertTrue("Find tab2_col1 failed",idx_pk >= 0);
            assertTrue("table1.AddFK fk1 before table2.AddColumn column1", idx_fk > idx_pk);
        }
        {   //Cycle reference
            m_plugin.clear();
            m_as.clear();
            TableDef tab1 = new TableDef("tab1");
            tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab1.addModification(2, new AddForeignKeyOperation("tab1_fk1", Arrays.asList("col1"), "tab2", "tab2_pk1"));
            tab1.addModification(3, new AddPrimaryKeyOperation("tab1_pk1", Arrays.asList("col1")));
            m_db.add(tab1);
            m_as.put("tab1", 0);
            TableDef tab2 = new TableDef("tab2");
            tab2.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab2.addModification(2, new AddForeignKeyOperation("tab2_fk1", Arrays.asList("col1"), "tab1", "tab1_pk1"));
            tab2.addModification(3, new AddPrimaryKeyOperation("tab2_pk1", Arrays.asList("col1")));
            m_db.add(tab2);
            m_as.put("tab2", 0);
            try{
                m_db.getCommandList();
                fail("must be cycle reference");
            } catch(ECycleReference e){
                if(e.m_dboName.equals("tab1")){
                    assertEquals("tab1_fk1", e.m_opName);
                } else {
                    assertEquals("tab2_fk1", e.m_opName);                    
                }                 
            }            
        }
        {   //unknow index in parent table
            m_plugin.clear();
            m_as.clear();
            TableDef tab1 = new TableDef("tab1");
            tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab1.addModification(2, new AddForeignKeyOperation("tab1_fk1", Arrays.asList("col1"), "tab2", "pk_unknown"));
            tab1.addModification(3, new AddPrimaryKeyOperation("tab1_pk1", Arrays.asList("col1")));
            m_db.add(tab1);
            m_as.put("tab1", 0);
            TableDef tab2 = new TableDef("tab2");
            tab2.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            m_db.add(tab2);
            m_as.put("tab2", 0);
            try{
                m_db.getCommandList();
                fail("must be unknown index");
            } catch(EUnknownReference e){
                assertEquals("tab1", e.m_dboName);
                assertEquals("tab1_fk1", e.m_opName);
            }            
        }
        {   //unknow parent table
            m_plugin.clear();
            m_as.clear();
            TableDef tab1 = new TableDef("tab1");
            tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab1.addModification(2, new AddForeignKeyOperation("tab1_fk1", Arrays.asList("col1"), "unknown_tab", "pk1"));
            m_db.add(tab1);
            m_as.put("tab1", 0);
            try{
                m_db.getCommandList();
                fail("must unknown table");
            } catch(EUnknownReference e){
                assertEquals("tab1", e.m_dboName);
                assertEquals("tab1_fk1", e.m_opName);
                assertEquals("unknown_tab", e.m_refName);
            }            
        }
        {   //Delete primary key before delete foreign key 
            m_plugin.clear();
            m_db = new DBTables(new TestAS(m_as), m_plugin);
            m_as.clear();
            TableDef tab1 = new TableDef("tab1");
            tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab1.addModification(2, new AddPrimaryKeyOperation("pk1", Arrays.asList("col1")));
            DeleteIndexOperation tab1_pk1 = new DeleteIndexOperation("pk1",IndexDef.TYPE.PRIMARY_KEY, false);
            tab1.addModification(3, tab1_pk1);
            m_db.add(tab1);
            m_as.put("tab1", 0);
            TableDef tab2 = new TableDef("tab2");
            tab2.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab2.addModification(2, new AddForeignKeyOperation("fk1", Arrays.asList("col1"), "tab1", "pk1"));
            DeleteIndexOperation tab2_fk1 = new DeleteIndexOperation("fk1",IndexDef.TYPE.FOREIGN_KEY, false);
            tab2.addModification(3, tab2_fk1);
            m_db.add(tab2);
            m_as.put("tab2", 0);

            List<DBOperation> cl = m_db.getCommandList();
            
            assertEquals(5, cl.size());
            int idx_pk = cl.indexOf(new DBOperation("tab1", tab1_pk1));
            int idx_fk = cl.indexOf(new DBOperation("tab2", tab2_fk1));
            assertTrue("Find tab1_fk1 failed",idx_pk >= 0);
            assertTrue("Find tab2_col1 failed",idx_fk >= 0);
            assertTrue("tab2.DelFK fk1 before tab1.DelPK pk1", idx_fk < idx_pk);
        }
        {   //Delete primary key, but not delete foreign key 
            m_plugin.clear();
            m_as.clear();
            TableDef tab1 = new TableDef("tab1");
            tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab1.addModification(2, new AddPrimaryKeyOperation("pk1", Arrays.asList("col1")));
            tab1.addModification(3, new DeleteIndexOperation("pk1", IndexDef.TYPE.PRIMARY_KEY, false));
            m_db.add(tab1);
            m_as.put("tab1", 0);
            TableDef tab2 = new TableDef("tab2");
            tab2.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab2.addModification(2, new AddForeignKeyOperation("fk1", Arrays.asList("col1"), "tab1", "pk1"));
            m_db.add(tab2);
            m_as.put("tab2", 0);

            try{
                m_db.getCommandList();
                fail("must raise exception");
            } catch(EUnknownReference e){
                assertEquals("tab2", e.m_dboName);
                assertEquals("fk1", e.m_opName);
            }             
        }
        {
            m_plugin.clear();
            m_as.clear();
            m_db = new DBTables(new TestAS(m_as), m_plugin);
            TableDef tab1 = new TableDef("tab1");
            tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab1.setDbName("must_ba_tab1");
            m_db.add(tab1);
            m_as.put("tab1", 0);
            DBOperation dbOp = m_db.getCommandList().get(0);
            CreateTableOperation createOp = (CreateTableOperation) dbOp.getOperation();
            assertEquals("must_ba_tab1", createOp.getDbName());
            
        }
        {
            m_plugin.clear();
            m_as.clear();
            TestAS testAS = new TestAS(m_as);
            m_db = new DBTables(testAS, m_plugin);
            TableDef tab1 = new TableDef("tab1");
            tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
            tab1.setDbName("external_tab1");
            tab1.setExternalTable(true);
            m_db.add(tab1);
            TableDef tab2 = new TableDef("tab2");
            tab2.setDbName("external_tab2");
            tab2.setExternalTable(true);
            m_db.add(tab2);
            
            assertTrue(m_db.getCommandList().isEmpty());
            
            assertEquals(null, testAS.getDBName("tab1"));
        }
        
        {
        	{ //maxKeySize test
	            TableDef tab = new TableDef("table1");
	            tab.addStringField(1, "f1", ColumnType.CHAR, 257, true, null);
	            tab.addIndex(2, "pk", new String[]{"f1"}, false, true);
	            m_db.add(tab);
	            m_db.setMaxIndexSize(200);
	            List<DBOperation> cl = m_db.getCommandList();
	            DBOperation op = cl.get(1);
	            assertTrue(op.getOperation() instanceof EmptyCommand);
	            
	            tab.deleteIndex(3, "pk");
	            cl = m_db.getCommandList();
	            op = cl.get(2);
	            assertTrue(op.getOperation().getClass().getSimpleName(), op.getOperation() instanceof EmptyCommand);
        	}
        	{
        		m_plugin.clear();
                m_as.clear();
                TestAS testAS = new TestAS(m_as);
                m_db = new DBTables(testAS, m_plugin);
	            TableDef tab = new TableDef("table1");
	            tab.addStringField(1, "f1", ColumnType.CHAR, 257, true, null);
	            tab.addPrimaryKey(2, "pk", new String[]{"f1"});
	            m_db.add(tab);
	            m_db.setMaxIndexSize(200);
	            List<DBOperation> cl = m_db.getCommandList();
	            DBOperation op = cl.get(0);
	            CreateTableOperation createOp = (CreateTableOperation) op.getOperation();
	            assertEquals(2, createOp.getElements().size()); // no primary key
	            assertTrue(createOp.getElements().get(1) instanceof EmptyCommand);
        	}
        	
        }
    }
    
    public void testDBTables(){
        DBTables tabs = new DBTables();
        Set<String> tabSet = tabs.list();
        assertTrue(tabSet.isEmpty());
    }
    
    public void testAdd() throws EDBObjectException{
        DBTables tabs = new DBTables();
        tabs.add(new TableDef("tab1"));
        Object[] tabSet = tabs.list().toArray();
        assertEquals(1, tabSet.length);
        assertEquals("tab1", tabSet[0]);
    }
    
    public void testGet() throws EDBObjectException{
        DBTables tabs = new DBTables();
        tabs.add(new TableDef("tab1"));
        tabs.add(new TableDef("tab2"));
        tabs.add(new TableDef("tab3"));
        assertEquals("tab2", tabs.get("tab2").getEntityName());
        assertNull(tabs.get("unknown"));
    }
    
    public void testSetActualState(){
        DBTables tabs = new DBTables();
        HashMap<String, Integer> as = new HashMap<String, Integer>();
        as.put("tab1", 21);
        as.put("tab2", 12);
        tabs.setActualState(new TestAS(as));
        assertEquals(21, tabs.getActualState("tab1"));
    }
    
    public void testRemove(){
        DBTables tabs = new DBTables();
        tabs.add(new TableDef("tab1"));
        tabs.add(new TableDef("tab2"));
        tabs.add(new TableDef("tab3"));
        
        tabs.remove("tab2");
        assertNull(tabs.get("tab2"));
        assertNotNull(tabs.get("tab1"));
        
        tabs.remove("unkTab");
    }
   
}
