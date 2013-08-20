/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.dml.Table;
import com.triniforce.db.dml.Table.Row;
import com.triniforce.db.dml.Table.Row.State;
import com.triniforce.db.dml.TableAdapter;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QTable;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.TFUtils;

/**
 * Business logic of ACTUAL_TABLE_STATES table
 * 
 */
public class ActualStateBL implements UpgradeRunner.IActualState{
    
    private static final String DBTABLE_NAME_PREFIX = "t_"; //$NON-NLS-1$
    
    public static final String ACT_STATE_TABLE = "actual_table_states"; //$NON-NLS-1$

    public static final String DBNAME = "dbname"; //$NON-NLS-1$

    public static final String APPNAME = "appname"; //$NON-NLS-1$

    public static final String VERSION = "version"; //$NON-NLS-1$
    
    public static class TIndexNames extends TableDef{    	
    	public static final FieldDef appName = FieldDef.createStringField("app_name", ColumnType.VARCHAR, 250, true, null);
    	public static final FieldDef dbName  = FieldDef.createStringField("db_name", ColumnType.VARCHAR, 64, true, null);
    	public TIndexNames() {
			addField(1, appName);
			addField(2, dbName);
			addPrimaryKey(3, "pk1", new String[]{appName.getName()});
			addIndex(4, "pk2", new String[]{dbName.getName()}, true, true);
		}
    }
    
//    public static class PQGetAll extends PrepSql{
//        @Override
//        public QStatement buildSql() {
//            return new QSelect().joinLast(new QTable(ACT_STATE_TABLE, "AS")//$NON-NLS-1$
//            .addCol(APPNAME).addCol(DBNAME).addCol(VERSION));
//        }
//        
//        static ResSet exec(IStmtContainer sc){
//            return sc.prepareStatement(PQGetAll.class).executeQuery();
//        }
//    }
    
    HashMap<String, Table.Row> m_state = new HashMap<String, Table.Row>();
    HashSet<String> m_dbNames = new HashSet<String>();

	private int m_maxIndexLength;
	TableDef m_asDef;
	TableDef m_asIndexDef;
	
	Table m_asTable;
	Table m_asIndexTable;
	
	HashMap<String, String> m_dbIndexNames  = new HashMap<String, String>();
	HashMap<String, String> m_appIndexNames = new HashMap<String, String>();
    
    
    public ActualStateBL(Connection conn) throws SQLException {
    	m_asDef = getASDef(UpgradeRunner.getDbType(conn));
    	m_asTable = new Table();
    	m_asTable.addColumn(m_asDef.getFields().findElement(APPNAME).getElement());
    	m_asTable.addColumn(m_asDef.getFields().findElement(DBNAME).getElement());
    	m_asTable.addColumn(m_asDef.getFields().findElement(VERSION).getElement());
    	
    	m_asIndexDef = new TIndexNames();
    	m_asIndexTable = new Table();
    	m_asIndexTable.addColumn(m_asIndexDef.getFields().findElement(TIndexNames.appName.getName()).getElement());
    	m_asIndexTable.addColumn(m_asIndexDef.getFields().findElement(TIndexNames.dbName.getName()).getElement());

    	m_maxIndexLength = 
    		UpgradeRunner.getDbType(conn).equals(DbType.FIREBIRD) ? 31 : 63;
    	if(! inited(conn) ){
    		init(conn);
    	}
//        getAll(conn);
    }
    
    private boolean inited(Connection conn) {
        try {
			getAll(conn);
		} catch (SQLException e) {
			return false;
		}
		return 
			getVersion(ACT_STATE_TABLE) == 5 && 
			getVersion(TIndexNames.class.getName()) == 4;
	}

	protected TableDef getASDef(DbType type){
    	ColumnType strType;
    	int strSize;
    	if(DbType.FIREBIRD.equals(type)){
    		strType = ColumnType.VARCHAR;
    		strSize = 250;
    	}
    	else{
    		strType = ColumnType.NVARCHAR;
    		strSize = 255;    		
    	}
    	
        TableDef tab = new TableDef(ACT_STATE_TABLE);
        tab.addModification(1, new AddColumnOperation(FieldDef
                .createStringField(DBNAME, strType, strSize,
                        true, null)));
        tab.addModification(2, new AddColumnOperation(FieldDef
                .createStringField(APPNAME, strType, strSize,
                        true, null)));
        tab.addModification(3, new AddColumnOperation(FieldDef
                        .createScalarField(VERSION, ColumnType.INT,
                                true, "0"))); //$NON-NLS-1$
        tab.addModification(4, new AddPrimaryKeyOperation(
                "PK1", Arrays.asList(APPNAME))); //$NON-NLS-1$
        tab.addModification(5,
                        new AddIndexOperation(
                                IndexDef
                                        .createIndex(
                                                "PK2", Arrays.asList(DBNAME), true, true, false))); //$NON-NLS-1$
        tab.setDbName(ACT_STATE_TABLE);
        return tab;
        
    }
    
    protected void init(Connection conn) throws SQLException{
//        int vAS;
//        DatabaseMetaData md = conn.getMetaData();
//        
//        ResultSet rs = md.getTables(conn.getCatalog(), null, ACT_STATE_TABLE,
//                new String[] { "TABLE" }); //$NON-NLS-1$
//        try{
//            //vAS = rs.next() ? getVersion(ACT_STATE_TABLE) : 0;
//        	vAS = getVersion(ACT_STATE_TABLE);
//        } finally {
//            if (rs != null)
//                rs.close();
//        }

//        if (vAS < 5) {
            DBTables tabs = new DBTables();
            tabs.add(m_asDef);
            tabs.add(m_asIndexDef);
            tabs.setActualState(this);
            List<DBOperation> cl = tabs.getCommandList();
            
            UpgradeRunner ur = new UpgradeRunner(conn, this); 
            ur.run(cl);
//        }
    }
    
    private void getAll(Connection conn) throws SQLException{
    	// Load Table names and versions
    	String cmd = new QSelect().joinLast(
        		new QTable(ACT_STATE_TABLE)
        		.addCol(APPNAME)
        		.addCol(DBNAME)
        		.addCol(VERSION)).toString();
    	PreparedStatement ps = conn.prepareStatement(cmd);
    	try{
    		TableAdapter ta = new TableAdapter();
	    	ResultSet rs = ps.executeQuery();
    		ta.load(m_asTable, rs);
    		for(int i=0; i<m_asTable.getSize(); i++){
    			Row row = m_asTable.getRow(i);
	            m_state.put((String)row.getField(0), row);
	            m_dbNames.add((String)row.getField(1));
    		}
	        rs.close();
    	} finally{
    		ps.close();
    	}
    	
    	// Load index names
    	cmd = new QSelect().joinLast(
        		new QTable(getDBName(TIndexNames.class.getName()))
        		.addCol(TIndexNames.appName)
        		.addCol(TIndexNames.dbName)).toString();
    	
    	ps = conn.prepareStatement(cmd);
    	try{
    		TableAdapter ta = new TableAdapter();
	    	ResultSet rs = ps.executeQuery();
    		ta.load(m_asIndexTable, rs);
    		for(int i=0; i<m_asIndexTable.getSize(); i++){
    			Row row = m_asIndexTable.getRow(i);
    			String appName = (String) row.getField(0);
    			String dbName = (String) row.getField(1);
    			TFUtils.assertTrue(null == m_dbIndexNames.put(dbName, appName), dbName);
    			TFUtils.assertTrue(null == m_appIndexNames.put(appName, dbName), appName);
    		}
	        rs.close();
    	} finally{
    		ps.close();
    	}
    }

    /**
     * Get database table name by application name
     * 
     * @param appName -
     *            table name in application
     * @return - database name
     * @throws SQLException - ?
     *             something wrong in SQL
     */
    public String getDBName(String appName){
        Row r  = m_state.get(appName);
        return null == r ? null : (String)r.getField(1);
    }

    /**
     * Get version of table
     * 
     * @param appName -
     *            application table name
     * @return - table version, if no such table then return 0
     * @throws SQLException
     */
    public int getVersion(String appName){
        Row r  = m_state.get(appName);
        return null == r ? 0 : ((Number)r.getField(2)).intValue();
    }

    /**
     * Generate name for new table
     * 
     * @param appTabName -
     *            application table name for we make database name
     * @return - new database name
     * @throws SQLException - ?
     *             something wrong in SQL
     */
    public String generateTabName(String appTabName) throws SQLException {
        String tabLocName = DBTABLE_NAME_PREFIX
                + getLocalTabName(appTabName).toLowerCase();
        String tabName = tabLocName;
        int i=1;
        while(m_dbNames.contains(tabName)){
            tabName = tabLocName + Integer.toString(i);
        }
        return tabName;
    }

    private String getLocalTabName(String appTabName) {
        String withOutter = appTabName.substring(appTabName.lastIndexOf('.') + 1);
        int innerIdx = withOutter.lastIndexOf('$');
        return innerIdx == -1 ? withOutter : withOutter.substring(innerIdx + 1);
    }

    /**
     * Is table in database
     * 
     * @param appTableName -
     *            application table name
     * @return - true if is
     * @throws SQLException
     */
    public boolean tableExists(String appTableName) throws SQLException {
        return getDBName(appTableName) != null;
    }

    /**
     * Add table in actual state
     * 
     * @param dbName -
     *            database name
     * @param tabName -
     *            application table name
     * @param v -
     *            start version
     * @throws SQLException
     */
    public void addTable(String dbName, String tabName, int v){
        Row r = m_asTable.newRow();
        r.setField(0, tabName);
        r.setField(1, dbName);
        r.setField(2, v);
        m_state.put(tabName, r);
        m_dbNames.add(dbName);
    }

    /**
     * Remove record from actual state
     * 
     * @param tabName -
     *            application table name
     * @throws SQLException
     */
    public void removeTable(String tabName){
        Row row = m_state.remove(tabName);
        if(null != row){
            m_dbNames.remove(row.getField(1));
            row.delete();
        }
    }

    /**
     * Change stored version on dv(+ or -)
     * 
     * @param tabName -
     *            application table name
     * @param dv -
     *            version += dv
     * @throws SQLException
     */
    public void changeVersion(String tabName, int dv){
        if (dv != 0) {
            Row row = m_state.get(tabName);
            if(null != row){ 
            	int prev = ((Number)row.getField(2)).intValue();
            	row.setField(2, prev+dv);
            }
        }
    }

    /**
     * Get versions of several tables
     * 
     * @param tabs -
     *            list of table names
     * @return - map<table name, version>
     * @throws SQLException
     */
    public HashMap<String, Integer> getVersionMap(Collection<String> tabs)
            throws SQLException {
        HashMap<String, Integer> res = new HashMap<String, Integer>();
        for (String tabName : tabs) {
            res.put(tabName, getVersion(tabName));
        }
        return res;
    }

    public String getAppName(String dbName) throws SQLException {
    	String res = null;
		for(int i=0; i<m_asTable.getSize(); i++){
			Row row = m_asTable.getRow(i);
			String v = (String) row.getField(1);
			if(dbName.toLowerCase().equals(v.toLowerCase())){
				res = (String) row.getField(0);
				break;
			}
		}
        return res;
    }

	public void addIndexName(String appName, String dbName) {
		TFUtils.assertTrue(null == m_appIndexNames.put(appName, dbName), appName);
		TFUtils.assertTrue(null == m_dbIndexNames.put(dbName, appName), dbName);
		Row row = m_asIndexTable.newRow();
		row.setField(0, appName);
		row.setField(1, dbName);
		ApiAlgs.getLog(this).info("ADD NAME: "+appName + "("+dbName+")");
		
	}

	public String generateIndexName(String appName) {
		String res = appName;
		
		if(appName.length() > m_maxIndexLength){
			int i=0;
			while(true){
				String num = i==0 ? "" : Integer.toString(i);
				String dbName = appName.substring(0, m_maxIndexLength-num.length()).toUpperCase()+num;
				if(!m_dbIndexNames.containsKey(dbName)){
					res = dbName;
					break;
				}
				i++;
			}
		}
		else{
			res = appName.toUpperCase();
		}
		return res;
	}

	public String getIndexDbName(String appName) {
		String res = m_appIndexNames.get(appName);
		if(null == res && !m_appIndexNames.containsKey(appName))
			res = appName;
		return res;
		
	}

	public void deleteIndexName(String appName) {
		String upperName = appName.toUpperCase();
		for(int i=0; i<m_asIndexTable.getSize(); i++){
			Row row = m_asIndexTable.getRow(i);
			String appVal = (String) row.getField(0);
			if(		!EnumSet.of(State.DELETED, State.CANCELED).contains(row.getState()) 
					&& 
					upperName.equals(appVal.toUpperCase())){
				
				ApiAlgs.getLog(this).info("DROP NAME: "+appVal);
				String dbVal = (String) row.getField(1);
				TFUtils.assertNotNull(m_appIndexNames.remove(appVal), appVal);
				TFUtils.assertNotNull(m_dbIndexNames.remove(dbVal), dbVal);
				row.delete();
			}
		}
	}
	

	public void flush(Connection con) throws SQLException {
		TableAdapter ta = new TableAdapter();
		ta.flush(con, m_asTable, m_asDef, ACT_STATE_TABLE);
		ta.flush(con, m_asIndexTable, m_asIndexDef, getDBName(TIndexNames.class.getName()));
	}
	
	public Set<String> getDbTableNames(){
		return Collections.unmodifiableSet(m_dbNames);
	}

	public String queryIndexName(String dbTabName) {
		return m_dbIndexNames.get(dbTabName);
	}

}

