/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import com.triniforce.db.ddl.UpgradeRunner.DbType;


public class ActualStateBLTest extends DDLTestCase {

    ActualStateBL m_as;

    static boolean m_bInit = false;

    public static final String PKG_NAME = "Test.ActualStateBL.";

    protected void setUp() throws Exception {
        
        super.setUp();
        
        if(!m_bInit){
            try{
                getConnection().prepareStatement("DELETE FROM ACTUAL_TABLE_STATES WHERE APPNAME LIKE 'Test.ActualStateBL.%'").execute();
            } catch(SQLException e){}
            m_bInit = true;
        }
        
        m_as = new ActualStateBL(getConnection());

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testActualStateBL() throws SQLException{
        assertEquals(5, m_as.getVersion(ActualStateBL.ACT_STATE_TABLE));
        assertEquals(ActualStateBL.ACT_STATE_TABLE, m_as.getDBName(ActualStateBL.ACT_STATE_TABLE));
    }

    public void testGetDBName() throws Exception {
        m_as.addTable("testGetDBName", PKG_NAME+"testGetDBName", 12);

        String dbName = m_as.getDBName(PKG_NAME+"testGetDBName");
        assertEquals("testGetDBName", dbName);
    }

    public void testGetVersion() throws Exception {
        m_as.addTable("testGetVersion", PKG_NAME+"testGetVersion", 12);

        int version = m_as.getVersion(PKG_NAME+"testGetVersion");
        assertEquals(12, version);
    }
    
    public void testGenerateTabName() throws Exception {
        String tabName = "result";
        String subPkg1 = "name1.name2.testName.", subPkg2 = "name1Name.", subPkg3="f1.", subPkg4 = "ex3.";
        
        String dbName = m_as.generateTabName(PKG_NAME+subPkg1+tabName);
        assertEquals("t_result", dbName);

        m_as.addTable(dbName, PKG_NAME+subPkg1+tabName, 12);
        String dbName2 = m_as.generateTabName(PKG_NAME+subPkg2+tabName);
        assertEquals("t_result1", dbName2);

        m_as.addTable("t_result2", PKG_NAME+subPkg3+tabName, 2);
        m_as.removeTable(PKG_NAME+subPkg2+tabName);
        assertEquals("t_result1", m_as.generateTabName(PKG_NAME+subPkg4+tabName));
        
        {//inner TableDef
            assertEquals("t_innerclass", m_as.generateTabName("pkg1.pkg11.pkg113.OuterClass$InnerClass"));
            assertEquals("t_inner2class", m_as.generateTabName("pkg1.pkg11.pkg113.OuterClass$InnerClass$Inner2Class"));
        }
    }

    public void testTableExists() throws Exception {
        m_as.addTable("testTableExists", PKG_NAME+"testTableExists", 12);
        assertTrue(m_as.tableExists(PKG_NAME+"testTableExists"));
        assertFalse(m_as.tableExists("unknown"));
    }

    public void testAddTable() throws Exception {
        m_as.addTable("testAddTable", PKG_NAME+"testAddTable", 12);
        assertEquals("testAddTable", m_as.getDBName(PKG_NAME+"testAddTable"));
        assertEquals(12, m_as.getVersion(PKG_NAME+"testAddTable"));
   }

    public void testRemoveTable() throws Exception {
        m_as.addTable("testRemoveTable", PKG_NAME+"testRemoveTable", 12);
        m_as.removeTable(PKG_NAME+"testRemoveTable");

        assertEquals(null, m_as.getDBName(PKG_NAME+"testRemoveTable"));
        assertEquals(0, m_as.getVersion(PKG_NAME+"testRemoveTable"));
    }

    public void testChangeVersion() throws Exception {
        m_as.addTable("testChangeVersion", PKG_NAME+"testChangeVersion", 12);
        m_as.changeVersion(PKG_NAME+"testChangeVersion", 4);
        assertEquals(12 + 4, m_as.getVersion(PKG_NAME+"testChangeVersion"));
        m_as.changeVersion(PKG_NAME+"testChangeVersion", -5);
        assertEquals(12 + 4 - 5, m_as.getVersion(PKG_NAME+"testChangeVersion"));

    }
    
    public void testGetVersionMap() throws Exception {
        m_as.addTable("DBTab1", PKG_NAME+"testGetVersionMap.DBTab1", 12);
        m_as.addTable("DBTab2", PKG_NAME+"testGetVersionMap.DBTab2", 15);
        m_as.addTable("DBTab3", PKG_NAME+"testGetVersionMap.DBTab3", 18);

        HashMap<String, Integer> vm = m_as.getVersionMap(Arrays.asList(
                PKG_NAME+"testGetVersionMap.DBTab1", PKG_NAME+"testGetVersionMap.DBTab3", "unknown_tab"));

        assertEquals(3, vm.size());
        assertEquals(12, vm.get(PKG_NAME+"testGetVersionMap.DBTab1").intValue());
        assertEquals(18, vm.get(PKG_NAME+"testGetVersionMap.DBTab3").intValue());
        assertEquals(0, vm.get("unknown_tab").intValue());
    }
    
    public void testGetAppName() throws Exception{
        m_as.addTable("testGetAppName", PKG_NAME+"testGetAppName", 12);
        assertEquals(PKG_NAME+"testGetAppName", m_as.getAppName("testGetAppName"));
        assertEquals(PKG_NAME+"testGetAppName", m_as.getAppName("TESTGETAPPNAME"));
        
    }
    
    public void testLoadState() throws Exception{
     /*   HashMap<String, String> names = new HashMap<String, String>(); 
        HashMap<String, Integer> vers = new HashMap<String, Integer>();
        
        m_as.loadState(vers, names);
        
        int oldSz = vers.size();
        
        m_as.addTable(getConnection(), "DBTab1", PKG_NAME+"testGetVersionMap.DBTab1", 12);
        m_as.addTable(getConnection(), "DBTab2", PKG_NAME+"testGetVersionMap.DBTab2", 15);
        m_as.addTable(getConnection(), "DBTab3", PKG_NAME+"testGetVersionMap.DBTab3", 18);

        vers.put("garbage", Integer.valueOf(1232));
        names.clear();
        
        m_as.loadState(vers, names);
        
        assertEquals(oldSz + 3, vers.size());
        assertEquals(oldSz + 3, names.size());
        
        assertEquals("DBTab2", names.get(PKG_NAME+"testGetVersionMap.DBTab2"));
        assertEquals(Integer.valueOf(18), vers.get(PKG_NAME+"testGetVersionMap.DBTab3"));
   */ }
    
    public void testIndexNames() throws Exception{
		assertEquals("APP1_NAME1", m_as.generateIndexName("app1_name1"));
		assertEquals("APP2_NAME2", m_as.generateIndexName("app2_name2"));
    	if(getDbType().equals(DbType.FIREBIRD)){
    		//------------1234567890123456789012345678901
    		assertEquals("APP1234567890_NAME1234567890_ID", m_as.generateIndexName("app1234567890_name1234567890_idx1"));
    		m_as.addIndexName("app1234567890_name1234567890_idx1", "APP1234567890_NAME1234567890_ID");
    		assertEquals("APP1234567890_NAME1234567890_ID", m_as.getIndexDbName("app1234567890_name1234567890_idx1"));
    		assertEquals("APP1234567890_NAME1234567890_I1", m_as.generateIndexName("app1234567890_NAME1234567890_idx1"));
    		
    		m_as.addIndexName("app1234567890_name1234567890_idx2", m_as.generateIndexName("app1234567890_name1234567890_idx2"));
    		m_as.addIndexName("app1234567890_name1234567890_idx3", m_as.generateIndexName("app1234567890_name1234567890_idx3"));
    		
    		m_as.deleteIndexName("app1234567890_name1234567890_idx2");
    		
    		assertEquals("app1234567890_name1234567890_idx2", m_as.getIndexDbName("app1234567890_name1234567890_idx2"));
    		assertEquals("APP1234567890_NAME1234567890_I2", m_as.getIndexDbName("app1234567890_name1234567890_idx3"));
    	}
    	else{
    		assertEquals("app1234567890_name1234567890_idx1".toUpperCase(), m_as.generateIndexName("app1234567890_name1234567890_idx1"));    		
    	}
    	
    	assertEquals("app1_1231", m_as.getIndexDbName("app1_1231"));
    }
    
    public void testFlush() throws Exception{
    	Connection con = getConnection();
    	m_as.addTable("dbname_testFlush", "app_name1", 12);
    	assertNotNull(m_as.getDBName("app_name1"));
    	{
    		// data is not flushed
	    	ActualStateBL as = new ActualStateBL(con);
	    	assertNull(as.getDBName("app_name1"));
    	}
    	{
    		// flush data
    		m_as.flush(con);
    		ActualStateBL as = new ActualStateBL(con);
	    	assertNotNull(as.getDBName("app_name1"));
    	}
    	
    	
    }
    
    public void testDeleteIndexName(){
    	m_as.addIndexName("testDeleteIndexName.Name1",m_as.generateIndexName("testDeleteIndexName.Name1"));
    	m_as.addIndexName("testDeleteIndexName.Name2",m_as.generateIndexName("testDeleteIndexName.Name2"));
    	m_as.addIndexName("testDeleteIndexName.Name3",m_as.generateIndexName("testDeleteIndexName.Name3"));
    	m_as.addIndexName("testDeleteIndexName.Name4",m_as.generateIndexName("testDeleteIndexName.Name4"));
    	
    	m_as.deleteIndexName("testDeleteIndexName.Name2");
    	m_as.addIndexName("testDeleteIndexName.Name2",m_as.generateIndexName("testDeleteIndexName.Name2"));
    	m_as.deleteIndexName("testDeleteIndexName.Name2");
    	
    	assertEquals("testDeleteIndexName.Name2", m_as.getIndexDbName("testDeleteIndexName.Name2")); // Not found
    	
    }

}
