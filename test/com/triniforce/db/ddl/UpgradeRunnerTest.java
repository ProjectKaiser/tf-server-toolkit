/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.ddl;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Random;

import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.TableDef.EReferenceError;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IElementDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.ddl.UpgradeRunner.IActualState;
import com.triniforce.db.qbuilder.QInsert;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QTable;
import com.triniforce.db.qbuilder.WhereClause;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class UpgradeRunnerTest extends DDLTestCase {
	
    public static final String UNICODE_PATTERN = "۞∑русскийڧüöäë面伴"; //$NON-NLS-1$

    static final String TAB_WITH_PK = "TestPlayer.testTabWithPK";
   
//	static boolean bInit = false;

    UpgradeRunner m_player;
    ActualStateBL m_as;

	private TableDef m_tab1;

	private int m_maxIdle;
    
    @Override
    protected void setUp() throws Exception {
    	m_maxIdle = getDataSource().getMaxIdle();
    	getDataSource().setMaxIdle(0);
    	
        super.setUp();
        
        m_as = new ActualStateBL(getConnection());
        m_player = new UpgradeRunner(getConnection(), m_as);
        
        m_tab1 = new TableDef(TAB_WITH_PK);
        m_tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true, "0")));
        m_tab1.addModification(2, new AddPrimaryKeyOperation("pk1", Arrays.asList("col1")));
        
//        if(!bInit){

            m_player.init();
            
            createTableIfNeeded(m_tab1, m_as);
            
//            bInit = true;
//        }
        
    }
    
    @Override
    protected void tearDown() throws Exception {
    	super.tearDown();
    	getDataSource().setMaxIdle(m_maxIdle);
    }
    
    public void testDBPlayer(){
    }
    
    public void testInit() throws Exception{
    	tearDown();
        //clearConnection();
        
//        bInit = false;
        setUp();
        
        String initTabs[] = {UpgradeRunner.ACT_STATE_TABLE};
        for (int i = 0; i < initTabs.length; i++) {
            String tabName = initTabs[i];
            assertTrue(containTable(tabName));
        }
        
        assertEquals(5, m_as.getVersion(UpgradeRunner.ACT_STATE_TABLE));
    }
    	
	public void testGetFieldDefString() throws Exception{
        if(getDbType() == UpgradeRunner.DbType.MSSQL){
            {
                FieldDef f = FieldDef.createScalarField("f_name", ColumnType.INT, true, "0");
                assertEquals("\"F_NAME\" INT DEFAULT 0 NOT NULL", m_player.getFieldDefString(f));
            }
            {
                FieldDef f = FieldDef.createStringField("f_name2", ColumnType.CHAR, 245, true, "default string");
                assertEquals("\"F_NAME2\" CHAR(245) DEFAULT 'default string' NOT NULL", m_player.getFieldDefString(f));
            }
            {
                FieldDef f = FieldDef.createDecimalField("f_name", 45, 21, false, null);
                assertEquals("\"F_NAME\" DECIMAL(45,21)", m_player.getFieldDefString(f));
            }            
        } 
        else if(getDbType() == UpgradeRunner.DbType.MYSQL){
            FieldDef f = FieldDef.createStringField("f_name3", ColumnType.NVARCHAR, 245, true, "default string");
            assertEquals("`F_NAME3` VARCHAR(245) character set utf8 DEFAULT 'default string' NOT NULL", m_player.getFieldDefString(f));
        }        	
        else if(getDbType() == UpgradeRunner.DbType.FIREBIRD){
            FieldDef f = FieldDef.createStringField("f_name3", ColumnType.NVARCHAR, 245, true, "default string");
            assertEquals("\"F_NAME3\" VARCHAR(245) character set UNICODE_FSS DEFAULT 'default string' NOT NULL", m_player.getFieldDefString(f));

            f = FieldDef.createStringField("f_name3", ColumnType.VARCHAR, 245, true, "default string");
            assertEquals("\"F_NAME3\" VARCHAR(245) character set ASCII DEFAULT 'default string' NOT NULL", m_player.getFieldDefString(f));
        }
        else{
            {
                FieldDef f = FieldDef.createScalarField("f_name", ColumnType.INT, true, "0");
                assertEquals("\"F_NAME\" INTEGER DEFAULT 0 NOT NULL", m_player.getFieldDefString(f));
            }
            {
                FieldDef f = FieldDef.createStringField("f_name2", ColumnType.CHAR, 245, true, "default string");
                assertEquals("\"F_NAME2\" CHAR(245) DEFAULT 'default string' NOT NULL", m_player.getFieldDefString(f));
            }
            {
                FieldDef f = FieldDef.createDecimalField("f_name", 45, 21, false, null);
                assertEquals("\"F_NAME\" DECIMAL(45,21)", m_player.getFieldDefString(f));
            }
            {
                FieldDef f = FieldDef.createScalarField("f_nullable_datetime", ColumnType.DATETIME, false);
                if(getDbType() == UpgradeRunner.DbType.MYSQL)
                	assertEquals("`F_NULLABLE_DATETIME` TIMESTAMP NULL DEFAULT NULL", m_player.getFieldDefString(f));
                else
                	assertEquals("\"F_NULLABLE_DATETIME\" TIMESTAMP", m_player.getFieldDefString(f));
            }
        }
        
        
        try{
        	m_player.getFieldDefString(FieldDef.createScalarField("Field1",ColumnType.INT, false, null));
        	//fail();
        }catch(RuntimeException e){
        	
        }
        
        if(getDbType().equals(DbType.FIREBIRD)){
        	
        	
        }
	}
	
    
    public void testGetConstraintString() throws Exception{
    	if(!getDbType().equals(DbType.MYSQL)){
	        {
	            AddPrimaryKeyOperation op = new AddPrimaryKeyOperation("pk1", Arrays.asList("col1", "col2"));
	            String s = m_player.getPrimaryKeyConstraintString("DBT1", op,false);
	            assertEquals(MessageFormat.format("CONSTRAINT {0} PRIMARY KEY (\"COL1\", \"COL2\")", getDbType().equals(DbType.MYSQL)? "" : m_player.getFullIndexName("DBT1", "pk1", IndexDef.TYPE.PRIMARY_KEY).toUpperCase()), s);
	        }
	        {
	            AddForeignKeyOperation op = new AddForeignKeyOperation("fk1", Arrays.asList("col1"), TAB_WITH_PK, "pk1", AddForeignKeyOperation.DELETE_RULE.CASCADE, AddForeignKeyOperation.UPDATE_RULE.NOT_SPECIFIED);
	            op.setRefColumns(Arrays.asList("col1"));
	            String s = m_player.getForeignKeyConstraintString("DBT1", op, false);
	            String dbIdxName = m_as.getIndexDbName(m_player.getFullIndexName("DBT1", "fk1", IndexDef.TYPE.FOREIGN_KEY));
	            assertEquals(MessageFormat.format("CONSTRAINT {0} FOREIGN KEY (\"COL1\") REFERENCES {1} (\"COL1\") ON DELETE CASCADE", dbIdxName, m_as.getDBName(TAB_WITH_PK)),s);            
	        }
	        
	        if(getDbType().equals(DbType.FIREBIRD))
	        {
	            AddForeignKeyOperation op = new AddForeignKeyOperation("fk1_too_long_name_for_foreign_key", Arrays.asList("col1"), TAB_WITH_PK, "pk1", 
	            		AddForeignKeyOperation.DELETE_RULE.CASCADE, AddForeignKeyOperation.UPDATE_RULE.NOT_SPECIFIED);
	            op.setRefColumns(Arrays.asList("col1"));
	            String s = m_player.getForeignKeyConstraintString("DBT1", op, false);
	            String dbIdxName = m_as.getIndexDbName(m_player.getFullIndexName("DBT1", "fk1_too_long_name_for_foreign_key", IndexDef.TYPE.FOREIGN_KEY));
	            assertEquals(MessageFormat.format("CONSTRAINT {0} FOREIGN KEY (\"COL1\") REFERENCES {1} (\"COL1\") ON DELETE CASCADE", dbIdxName, m_as.getDBName(TAB_WITH_PK)), s);            
	        }
    	}
    }
    
    public void testRunCreateTable() throws Exception{
    	String testTabName = "TestPlayer.testRunCreateTable" + (new Random().nextInt(1000));
    	{
	    	String tabName = testTabName;
	    	ArrayList<TableUpdateOperation> nodes = new ArrayList<TableUpdateOperation>();
	    	nodes.add(new AddColumnOperation(FieldDef.createScalarField("int_field", ColumnType.INT, true)));
	    	nodes.add(new AddColumnOperation(FieldDef.createStringField("string_field", ColumnType.CHAR, 128, true, "default str")));
	    	nodes.add(new AddColumnOperation(FieldDef.createDecimalField("dec_field", 4, 2, false, null)));
	    	nodes.add(new AddPrimaryKeyOperation("pk1", Arrays.asList("int_field", "string_field")));
	    	
	    	ArrayList<DBOperation> cl = new ArrayList<DBOperation>();
	    	cl.add(new DBOperation(tabName, new CreateTableOperation(nodes)));
	
	    	m_player.run(cl);
	        getConnection().commit();
	    	
	    	assertEquals(4, m_player.getActualState().getVersion(tabName));
	    	assertTrue("no table in AS", m_player.getActualState().tableExists(tabName));
	        String dbName = m_as.getDBName(tabName);
	        assertTrue(containTable(dbName));
	        assertNotNull(getField(dbName, "int_field"));
	        assertNotNull(getField(dbName, "string_field"));
	        assertNotNull(getField(dbName, "dec_field"));
	        assertNotNull(getPrimaryKey(dbName, m_player.getFullIndexName(dbName, "pk1", IndexDef.TYPE.PRIMARY_KEY)));
    	}
        {	// test DBName set
        	String tabName = testTabName+ "_2";
        	ArrayList<TableUpdateOperation> nodes = new ArrayList<TableUpdateOperation>();
        	nodes.add(new AddColumnOperation(FieldDef.createScalarField("int_field", ColumnType.INT, true)));
        	CreateTableOperation op = new CreateTableOperation(nodes);
        	String hardDbName = "hardcoded_name"+(new Random().nextInt(1000)); 
        	op.setDbName(hardDbName);
        	ArrayList<DBOperation> cl = new ArrayList<DBOperation>();
        	cl.add(new DBOperation(tabName, op));
        	m_player.run(cl);
        	assertEquals(hardDbName, 
        			m_player.getActualState().getDBName(tabName));
        	
        }
    }
    
    class FieldGreaterIndex implements Comparator<IElementDef>{

		public int compare(IElementDef o1, IElementDef o2) {
			if(o1.getClass().equals(o2.getClass())) 
				return o1.getName().compareTo(o2.getName());
			else{				
				if(o1 instanceof FieldDef)
					return -1;
				else 
					return 1;
			}
		}
   	
    }        
    class FooNode implements IElementDef{
    	String f;
    	FooNode(String af){
    		f = af;
    	}
		public String getName() {
			return f;
		}        	
    }
    
    class NameComparator implements Comparator<IElementDef>{

        public int compare(IElementDef arg0, IElementDef arg1) {
            return arg0.getName().toUpperCase().compareTo(arg1.getName().toUpperCase());
        }
    }
    
    protected void assertTabNodes(String tabName, ArrayList<IElementDef> nodes) throws Exception {
		Collections.sort(nodes, new FieldGreaterIndex());
		int fNum=0;
		for (; fNum < nodes.size() && nodes.get(fNum) instanceof FieldDef; fNum++);
		List<IElementDef> fields = nodes.subList(0, fNum);
//		List<INodeDef> indices = nodes.subList(fNum, nodes.size());
		
		ResultSet rs = getConnection().getMetaData().getColumns(null, null, m_as.getDBName(tabName).toUpperCase(), null);
		fNum = 0;
		while(rs.next()){
			String fName = rs.getString("COLUMN_NAME");
			int idx = Collections.binarySearch(fields, new FooNode(fName.toUpperCase()), new NameComparator());
			assertTrue("Field not found: "+ fName, idx >= 0);
			FieldDef f = (FieldDef)fields.get(idx);
//			assertEquals("check type. Field: "+fName, f.m_type, FieldDef.ddlType(rs.getInt("DATA_TYPE")));
			if(FieldDef.isDecimalType(f.m_type)){
				assertEquals("check size. Field: "+fName, f.m_size, rs.getInt("COLUMN_SIZE"));
				assertEquals("check scale. Field: "+fName, f.m_scale, rs.getInt("DECIMAL_DIGITS"));
			}
//			if(FieldDef.isStringType(f.m_type)){
//				assertEquals("check size. Field: "+fName, f.m_size, rs.getInt("COLUMN_SIZE"));
//			}
 			assertEquals("check nullable. Field: "+fName, f.m_bNotNull, rs.getInt("NULLABLE")!=DatabaseMetaData.columnNullable );
			fNum++;	
		}
		assertEquals("check field number", fields.size(), fNum);
	}
    
    public void testRunDropTable() throws Exception{
    	String tabName = "TestPlayer.testRunDropTable_"+(new Random().nextInt(1000));
        ArrayList<TableUpdateOperation> nodes = new ArrayList<TableUpdateOperation>();
        nodes.add(new AddColumnOperation(FieldDef.createScalarField("int_field", ColumnType.INT, true)));
        nodes.add(new AddColumnOperation(FieldDef.createStringField("string_field", ColumnType.CHAR, 128, true, "''")));
        nodes.add(new AddColumnOperation(FieldDef.createDecimalField("dec_field", 4, 2, false, null)));
        nodes.add(new AddPrimaryKeyOperation("pk1", Arrays.asList("int_field", "string_field")));
        
        ArrayList<DBOperation> cl = new ArrayList<DBOperation>();
        CreateTableOperation createOp = new CreateTableOperation(nodes);
        cl.add(new DBOperation(tabName, createOp));

        m_player.run(cl);
        
    	String dbName = m_player.getActualState().getDBName(tabName);
    			
    	cl.clear();
    	cl.add(new DBOperation(tabName, createOp.getReverseOperation()));
    	m_player.run(cl);  
    	
    	IActualState as = m_player.getActualState();
    	assertFalse("tab in AS", as.tableExists(tabName));
    	assertFalse("tab in base", containTable(dbName));
    	
    	//Remove index names
    	String idxName = dbName+"_pk1";
    	as.addIndexName(idxName, "anything"); // Allow, value
    }
    
    public void testRunEditTable() throws Exception{
		String tabName = "TestPlayer.testRunEdit_" + (new Random().nextInt(1000));
		String dbName = m_as.generateTabName(tabName);
        ArrayList<TableUpdateOperation> nodes = new ArrayList<TableUpdateOperation>();
        nodes.add(new AddColumnOperation(FieldDef.createScalarField("int_field", ColumnType.INT, true)));
        ArrayList<DBOperation> cl = new ArrayList<DBOperation>();
        cl.add(new DBOperation(tabName, new CreateTableOperation(nodes)));
        m_player.run(cl);
    	
    	{ // alter table add column
    		int v = m_player.getActualState().getVersion(tabName);
    		FieldDef f = FieldDef.createStringField("added_column", ColumnType.VARCHAR, 24, false, null);
    		cl.clear();
    		cl.add(new DBOperation(tabName, new AddColumnOperation(f)));
    		m_player.run(cl);
    		assertTrue(containField(m_as.getDBName(tabName), f));
    		assertEquals("version increase failed", v+1, m_player.getActualState().getVersion(tabName));
    	}
    	{ //alter table add primary key
    		AddPrimaryKeyOperation op = new AddPrimaryKeyOperation("pk1", Arrays.asList("int_field"));
    		cl.clear();
    		cl.add(new DBOperation(tabName, op));
    		m_player.run(cl);
    		IndexDef pk = getPrimaryKey(dbName, m_player.getFullIndexName(dbName, "pk1", IndexDef.TYPE.PRIMARY_KEY));
    		assertNotNull(pk);
    		assertEquals(1, pk.m_columns.size());
    		assertEquals("int_field", pk.m_columns.get(0));
    	}
    	{ //alter table add foreign key
    		AddForeignKeyOperation op = new AddForeignKeyOperation("fk1", Arrays.asList("int_field"), TAB_WITH_PK, "pk1");
            op.setRefColumns(Arrays.asList("col1"));
    		cl.clear();
    		cl.add(new DBOperation(tabName, op));
    		ApiAlgs.getLog(this).trace("numIdle : " + getDataSource().getNumIdle());
    		ApiAlgs.getLog(this).trace("numActive : " + getDataSource().getNumActive());
    		getDataSource().setMaxIdle(0);
    		m_player.run(cl);
    		String dbRefTab = m_as.getDBName(TAB_WITH_PK).toUpperCase();
    		IndexDef fk = getForeignKey(dbName, dbName+"_fk1");
    		assertNotNull("FK not found", fk);
    		assertEquals(dbRefTab, fk.m_parentTable);
            if(getDbType()!=DbType.MYSQL && getDbType()!=DbType.HSQL && getDbType()!=DbType.H2 )
                assertEquals(dbRefTab+"_PK1", fk.m_parentIndex);
    		assertEquals(1, fk.m_columns.size());
    		assertEquals("INT_FIELD", fk.m_columns.get(0));
    	}
    	{ //alter table delete constraint
    		DeleteIndexOperation op = new DeleteIndexOperation("fk1", IndexDef.TYPE.FOREIGN_KEY, false);
    		cl.clear();
    		cl.add(new DBOperation(tabName, op));
    		m_player.run(cl);
    		assertNull(getForeignKey(dbName, dbName+"_fk1"));
    	}
    	{ //alter table delete column 
    		DeleteColumnOperation op = new DeleteColumnOperation(FieldDef.createStringField("added_column", ColumnType.VARCHAR, 24, false, null));
    		cl.clear();
    		cl.add(new DBOperation(tabName, op));
    		m_player.run(cl);
    		FieldDef f = getField(dbName, "added_column");
            if(m_player.m_bSupportDropColumn)
                assertNull(f);
            else
                assertFalse(f.m_bNotNull);
    	}
    	
    	{//alter table delete primary key
    		DeleteIndexOperation op = new DeleteIndexOperation("pk1", IndexDef.TYPE.PRIMARY_KEY, true);
    		cl.clear();
    		cl.add(new DBOperation(tabName, op));
    		m_player.run(cl);    		
    	}
    }
    
    public void testGetOperationString() throws Exception{
		String tabName = "testPlayer.testGetOperationString";
		String dbName = m_as.generateTabName(tabName);
		int v = 3;
		m_as.addTable(dbName, tabName, v);
    	{
    		String fName = "added_column";
            FieldDef f = FieldDef.createStringField(fName, ColumnType.VARCHAR, 24, false, null);
        	AddColumnOperation op = new AddColumnOperation(f);        	
    		String sql = m_player.getOperationString(new DBOperation(tabName, op));
    		assertEquals(MessageFormat.format("ALTER TABLE {0} ADD {1}", dbName, m_player.getFieldDefString(f)), sql);
    	}
    	{
    		String fName = "INT_FIELD", kName = "pk1";    		
    		AddPrimaryKeyOperation op = new AddPrimaryKeyOperation(kName, Arrays.asList(fName));
    		String sql = m_player.getOperationString(new DBOperation(tabName, op));
    		assertEquals(MessageFormat.format("ALTER TABLE {0} ADD {1}",dbName, m_player.getPrimaryKeyConstraintString(dbName, op, true)), sql);
    	}
    	{    		
    		String fName = "INT_FIELD", fkName = "fk62", pkName="pk1";
    		AddForeignKeyOperation op = new AddForeignKeyOperation(fkName, Arrays.asList(fName), TAB_WITH_PK, pkName);    		
            op.setRefColumns(Arrays.asList("col1"));
    		String sql = m_player.getOperationString(new DBOperation(tabName, op));    		    		
            assertEquals(MessageFormat.format("ALTER TABLE {0} ADD {1}",dbName, m_player.getForeignKeyConstraintString(dbName, op, true)), sql);
    	}
    	{
    		String fName = "col1";
    		DeleteColumnOperation op = new DeleteColumnOperation(fName);
    		String sql = m_player.getOperationString(new DBOperation(tabName, op));    		
            if(getDbType().equals(DbType.FIREBIRD))
                assertEquals(MessageFormat.format("ALTER TABLE {0} DROP \"{1}\"", dbName, fName.toUpperCase()), sql);
            else
                assertEquals(MessageFormat.format("ALTER TABLE {0} DROP COLUMN {1}", dbName, getQuoted(fName.toUpperCase())), sql);
    	}
    	{
            if(getDbType().equals(DbType.MYSQL)){
                {
                    String fkName = "fk1";
                    DeleteIndexOperation op = new DeleteIndexOperation(fkName, IndexDef.TYPE.PRIMARY_KEY, true);
                    String sql = m_player.getOperationString(new DBOperation(tabName, op));         
                    assertEquals(MessageFormat.format("ALTER TABLE {0} DROP PRIMARY KEY",dbName), sql);
                }
                {
                    String fkName = "fk1";
                    DeleteIndexOperation op = new DeleteIndexOperation(fkName, IndexDef.TYPE.FOREIGN_KEY, false);
                    String sql = m_player.getOperationString(new DBOperation(tabName, op));         
                    assertEquals(MessageFormat.format("ALTER TABLE {0} DROP FOREIGN KEY {0}_{1}",dbName, fkName), sql);
                }
            }
            else{
        		String fkName = "fk1";
        		DeleteIndexOperation op = new DeleteIndexOperation(fkName, IndexDef.TYPE.FOREIGN_KEY, false);
        		String sql = m_player.getOperationString(new DBOperation(tabName, op));    		
        		assertEquals(MessageFormat.format("ALTER TABLE {0} DROP CONSTRAINT {0}_{1}",dbName, fkName), sql);
            }
    	}
        ArrayList<TableUpdateOperation> nodes = new ArrayList<TableUpdateOperation>();
        nodes.add(new AddColumnOperation(FieldDef.createScalarField("int_field", ColumnType.INT, true)));
        nodes.add(new AddColumnOperation(FieldDef.createStringField("string_field", ColumnType.CHAR, 128, true, "''")));
        nodes.add(new AddColumnOperation(FieldDef.createDecimalField("dec_field", 4, 2, false, null)));
        nodes.add(new AddPrimaryKeyOperation("pk1", Arrays.asList("int_field, string_field")));
    	{
            
            CreateTableOperation op = new CreateTableOperation(nodes);
            
        	String sql = m_player.getOperationString(new DBOperation(tabName, op));
        	assertEquals(null, sql);    		
    	}
    	{
        	DropTableOperation op = new DropTableOperation(nodes);
        	String sql = m_player.getOperationString(new DBOperation(tabName, op));
        	assertEquals(MessageFormat.format("DROP TABLE {0}", dbName), sql);    		    		
    	}
        {
            AlterColumnOperation op = new AlterColumnOperation(
                    FieldDef.createScalarField("col1", ColumnType.FLOAT, true), 
                    FieldDef.createScalarField("col1", ColumnType.INT, false));
            String sql = m_player.getOperationString(new DBOperation(tabName, op));
            if(getDbType().equals(DbType.MYSQL)){
            	assertEquals(MessageFormat.format("ALTER TABLE {0} MODIFY COLUMN {1} {2} NULL", dbName, "col1", m_player.getTypeString(ColumnType.INT, 0, 0)), sql);
            }
            else if(getDbType().equals(DbType.DERBY)){
            	assertEquals(MessageFormat.format("ALTER TABLE {0} ALTER COLUMN {1} SET DATA TYPE {2}", dbName, "col1", m_player.getTypeString(ColumnType.INT, 0, 0)), sql);
            }else{
            	assertEquals(MessageFormat.format("ALTER TABLE {0} ALTER COLUMN {1} {2} NULL", dbName, "col1", m_player.getTypeString(ColumnType.INT, 0, 0)), sql);
            }
        }
        {
            DeleteDefaultConstraintOperation op = new DeleteDefaultConstraintOperation("DEFAULT_CONSTRAINT", "COLUMN1", "TEMPLATE");
            String sql = m_player.getOperationString(new DBOperation(tabName, op));
            assertEquals(MessageFormat.format("ALTER TABLE {0} DROP CONSTRAINT DEFAULT_CONSTRAINT", dbName), sql);                                  
        }
        {
            AddDefaultConstraintOperation op = new AddDefaultConstraintOperation("DEFAULT_CONSTRAINT", "COLUMN1", "TEMPLATE");
            String sql = m_player.getOperationString(new DBOperation(tabName, op));
            assertEquals(MessageFormat.format("ALTER TABLE {0} ADD CONSTRAINT DEFAULT_CONSTRAINT DEFAULT ''TEMPLATE'' FOR COLUMN1", dbName), sql);                                  
        }
        {
            String fName = "nullable_datetime_column";
            FieldDef f = FieldDef.createScalarField(fName, ColumnType.DATETIME, false);
            AddColumnOperation op = new AddColumnOperation(f);          
            String sql = m_player.getOperationString(new DBOperation(tabName, op));
            assertEquals(MessageFormat.format("ALTER TABLE {0} ADD {1}", dbName, m_player.getFieldDefString(f)), sql);
        }
        {
        	AddIndexOperation op = new AddIndexOperation(IndexDef.createIndex("nuIndex", Arrays.asList("INT_FIELD"), true, true, false));
            String sql = m_player.getOperationString(new DBOperation(tabName, op));
            assertEquals(MessageFormat.format("ALTER TABLE {0} ADD CONSTRAINT {1} UNIQUE ({2})", 
            		dbName, m_as.getIndexDbName(m_player.getFullIndexName(dbName, "nuIndex", IndexDef.TYPE.INDEX)), getQuoted("INT_FIELD")), sql);
        }
        {
        	HashMap<String, String> opts = new HashMap<String, String>();
        	opts.put("Option1", "OFF");
        	opts.put("Option5", "ON");
        	AddIndexOperation op = new AddIndexOperation(IndexDef.createIndex("nuIndex2", Arrays.asList("INT_FIELD"), false, true, false, 
        			Arrays.asList("Col12", "Col13"), opts));
        	String sql = m_player.getOperationString(new DBOperation(tabName, op));
        	assertEquals(
        			MessageFormat.format("CREATE   INDEX {0} ON {1} ({2}) INCLUDE ({3}, {4}) WITH (Option1=OFF, Option5=ON)",
        					m_as.getIndexDbName(m_player.getFullIndexName(dbName, "nuIndex2", IndexDef.TYPE.INDEX)), 
        					dbName,
        					getQuoted("INT_FIELD"),
        					getQuoted("COL12"),
        					getQuoted("COL13") ),        					
        			sql);
        	
            
        }
        {
        	
        	AddIndexOperation op = new AddIndexOperation(IndexDef.createIndex("ORIGINAL_INDEX_NAME_91249149291343", Arrays.asList("INT_FIELD"), false, true, false, 
        			null, null, true));
        	String sql = m_player.getOperationString(new DBOperation(tabName, op));
        	IActualState as = m_player.getActualState();
        	assertEquals("ORIGINAL_INDEX_NAME_91249149291343", 
        			as.getIndexDbName(m_player.getFullIndexName(as.getDBName(tabName), "ORIGINAL_INDEX_NAME_91249149291343", IndexDef.TYPE.INDEX)));
        	assertEquals("CREATE   INDEX ORIGINAL_INDEX_NAME_91249149291343 ON t_testgetoperationstring (\"INT_FIELD\")", sql);
        }
		m_as.removeTable(tabName);    	    	
    }
    
    private Object getQuoted(String v) {
		return String.format("%1$s%2$s%1$s", ApiStack.getInterface(IDatabaseInfo.class).getIdentifierQuoteString(), v);
	}

	public void testRun() throws Exception{
    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
    	String t1Name = "TestPlayer.testRun1", t1DBName;
    	String t2Name = "TestPlayer.testRun2", t2DBName;
    	String t3Name = "TestPlayer.testRun3", t3DBName;
    	String t4Name = "TestPlayer.testRun4";
		TableDef t1, t2, t3;
    	{	//t1 = (col1, col2, pk1)
    		t1 = new TableDef(t1Name);
    		t1.setSupportForeignKeys(true);
    		desired.put(t1.getEntityName(), t1);
    		t1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
    		t1.addModification(2, new AddColumnOperation(FieldDef.createScalarField("col2", ColumnType.SMALLINT, true)));
    		t1.addModification(3, new AddPrimaryKeyOperation("pk1", Arrays.asList("col1","col2")));
        	DBTables db = new DBTables(m_as, desired);
    		List<DBOperation> cl = db.getCommandList();
    		m_player.run(cl);
            getConnection().commit();
    		
    		t1DBName = m_as.getDBName(t1.getEntityName());
    		assertTrue(containTable(t1DBName));
			assertNotNull(getField(t1DBName, "col1"));
			assertNotNull(getField(t1DBName, "col2"));
			assertNotNull(getPrimaryKey(t1DBName, m_player.getFullIndexName(m_as.getDBName(t1.getEntityName()),"pk1", IndexDef.TYPE.PRIMARY_KEY)));
			assertEquals(3, m_as.getVersion(t1.getEntityName()));
    	}
    	{	//t1 = (col1, col3, fk1);	t2 = (col1, pk1)
    		t2 = new TableDef(t2Name);
    		t1.setSupportForeignKeys(true);
    		desired.put(t2.getEntityName(), t2);
    		
    		t2.addModification(1, new AddColumnOperation(FieldDef.createStringField("col1", ColumnType.CHAR, 24, true, "''")));
    		t2.addModification(2, new AddPrimaryKeyOperation("pk1", Arrays.asList("col1")));
    		t1.addModification(4, new DeleteIndexOperation("pk1", IndexDef.TYPE.PRIMARY_KEY, false));
    		t1.addModification(5, new DeleteColumnOperation(FieldDef.createStringField("col2", ColumnType.CHAR, 24, true, "''")));
    		t1.addModification(6, new AddColumnOperation(FieldDef.createStringField("col3", ColumnType.CHAR, 24, false, null)));
    		t1.addModification(7, new AddForeignKeyOperation("fk1", Arrays.asList("col3"), t2.getEntityName(), "pk1"));
    		
        	DBTables db = new DBTables(m_as, desired);
    		List<DBOperation> cl = db.getCommandList();
    		m_player.run(cl);
            getConnection().commit();

    		t2DBName = m_as.getDBName(t2.getEntityName());
    		assertNotNull(t2DBName);
    		assertTrue(containTable(t2DBName));
    		assertNotNull(getField(t2DBName, "col1"));
    		assertNotNull(getPrimaryKey(t2DBName, m_player.getFullIndexName(t2DBName, "pk1", IndexDef.TYPE.PRIMARY_KEY)));
    		assertNull(getPrimaryKey(t1DBName, m_player.getFullIndexName(t1DBName, "pk1", IndexDef.TYPE.PRIMARY_KEY)));
    		assertNotNull(getField(t1DBName, "col3"));
    		assertNotNull(getForeignKey(t1DBName, t1DBName+"_fk1"));
    		assertEquals(7, m_as.getVersion(t1.getEntityName()));
    		assertEquals(2, m_as.getVersion(t2.getEntityName()));
    	}
    	{	//t3 = (col1, pk)
    		t3 = new TableDef(t3Name);
    		t1.setSupportForeignKeys(true);
    		desired.put(t3.getEntityName(), t3);
    		t3.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
    		t3.addModification(2, new AddColumnOperation(FieldDef.createScalarField("col2", ColumnType.SMALLINT, true)));
    		t3.addModification(3, new AddPrimaryKeyOperation("pk1", Arrays.asList("col1")));
    		t3.addModification(4, new DeleteColumnOperation(FieldDef.createScalarField("col2", ColumnType.SMALLINT, true)));
        	DBTables db = new DBTables(m_as, desired);
    		List<DBOperation> cl = db.getCommandList();
    		m_player.run(cl);
            getConnection().commit();
    		
    		t3DBName = m_as.getDBName(t3.getEntityName());
    		FieldDef f = getField(t3DBName, "col2");
            
            if(m_player.m_bSupportDropColumn)
                assertNull(f);
            else
                assertFalse(f.m_bNotNull);
    	}
    	{//test batch
    		int vt1 = m_as.getVersion(t1.getEntityName());
    		ArrayList<DBOperation> cl = new ArrayList<DBOperation>();
    		cl.add(new DBOperation(t1.getEntityName(), new AddColumnOperation(FieldDef.createScalarField("not_added_column", ColumnType.INT, true, "123"))));
    		ArrayList<TableUpdateOperation> elements  = new ArrayList<TableUpdateOperation>();
    		elements.add(new AddColumnOperation(FieldDef.createStringField("t4_col1", ColumnType.CHAR, 12, false, null)));
    		cl.add(new DBOperation("TestPlayer.testRun4", new CreateTableOperation(elements)));
    		cl.add(new DBOperation(t1.getEntityName(), new DeleteColumnOperation(FieldDef.createScalarField("unknown_column",ColumnType.INT, true))));

    		try{
    			m_player.run(cl);
    			fail("delete unknown column must raised"); 
    		} catch(SQLException e){
    			assertNull(m_as.getDBName("TestPlayer.testRun4"));
    			assertFalse(containTable("testRun4"));
        		FieldDef f = getField(t1DBName, "not_added_column");
                if(m_player.m_bSupportDropColumn)
                    assertNull(f);
                else
                    assertFalse(f.m_bNotNull);
    			assertEquals(vt1, m_as.getVersion(t1.getEntityName()));
    		}    		    		
    	}
        {
            int vt1 = m_as.getVersion(t1.getEntityName());
            ArrayList<DBOperation> cl = new ArrayList<DBOperation>();
            cl.add(new DBOperation(t1.getEntityName(), new DeleteColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true, "0"))));
            cl.add(new DBOperation(t1.getEntityName(), new DeleteColumnOperation(FieldDef.createScalarField("unknown_column", ColumnType.INT, true, "0"))));
            try{
                m_player.run(cl);
                fail("delete unknown column must raised"); 
            } catch(SQLException e){
                FieldDef f = getField(t1DBName, "col1");
                if(getDbType().equals(DbType.MSSQL))
                    assertNotNull(f);
                else if(getDbType().equals(DbType.DERBY))
                    assertTrue(f.m_bNotNull);
                assertEquals(vt1, m_as.getVersion(t1.getEntityName()));
            }
        }
        
        if(getDbType().equals(DbType.MSSQL)){	//t3 = (col1, index)
    		TableDef t4 = new TableDef(t4Name);
    		desired.put(t4.getEntityName(), t4);
    		t4.addModification(1, new AddColumnOperation(FieldDef.createScalarField("col1", ColumnType.INT, true)));
    		t4.addModification(2, new AddColumnOperation(FieldDef.createScalarField("col2", ColumnType.SMALLINT, true)));
    		HashMap<String, String> opts = new HashMap<String,String>();
    		opts.put("SORT_IN_TEMPDB", "OFF");
    		opts.put("ONLINE", "OFF");
    		t4.addModification(3, new AddIndexOperation(IndexDef.createIndex("IDX_COMPLEX1", Arrays.asList("col1"), false, 
    				true, false, Arrays.asList("col2"), opts)));
        	DBTables db = new DBTables(m_as, desired);
    		List<DBOperation> cl = db.getCommandList();
    		m_player.run(cl);
    	}    
    	
	}
    
    public void testGetCreateOperationString() throws Exception
	{
        FieldDef f1 = FieldDef.createScalarField("int_field", ColumnType.INT, true),
            f2 = FieldDef.createStringField("string_field", ColumnType.CHAR, 256, true, "default string"),
            f3 = FieldDef.createDecimalField("dec_field", 4, 2, false, null);
        AddPrimaryKeyOperation pk = new AddPrimaryKeyOperation("pk1", Arrays.asList("int_field, string_field")); 
    	ArrayList<TableUpdateOperation> nodes = new ArrayList<TableUpdateOperation>();
    	nodes.add(new AddColumnOperation(f1));
    	nodes.add(new AddColumnOperation(f2));
    	nodes.add(new AddColumnOperation(f3));
    	nodes.add(pk);
    	CreateTableOperation op = new CreateTableOperation(nodes);
    	String tabName = "TestPlayer.testGetCreateOperationString";
    	String dbName = m_as.generateTabName(tabName);
    	String sql = m_player.getCreateOperationString(dbName,op);

        if(getDbType().equals(DbType.MYSQL)){        
            assertEquals(MessageFormat.format("CREATE TABLE {0} ({1}, {2}, {3}, {4}) ENGINE = INNODB", dbName, m_player.getFieldDefString(f1), m_player.getFieldDefString(f2), m_player.getFieldDefString(f3), m_player.getPrimaryKeyConstraintString(dbName, pk,true)), sql);
        }else{
            assertEquals(MessageFormat.format("CREATE TABLE {0} ({1}, {2}, {3}, {4})", dbName, m_player.getFieldDefString(f1), m_player.getFieldDefString(f2), m_player.getFieldDefString(f3), m_player.getPrimaryKeyConstraintString(dbName, pk, true)), sql);
        }
	}
    
    public void testGetDbType() throws Exception{
        assertEquals(getDbType(), UpgradeRunner.getDbType(getConnection()));
    }
    
    public void testDatabaseTypes() throws Exception{
        
        FieldDef[] fields = new FieldDef[]{
                FieldDef.createScalarField("fint", ColumnType.INT, true),
                FieldDef.createScalarField("fsmallint", ColumnType.SMALLINT, true),
                FieldDef.createScalarField("ffloat", ColumnType.FLOAT, true),
                FieldDef.createScalarField("ftimestamp", ColumnType.DATETIME, false),
                FieldDef.createDecimalField("fdecimal_10_2", 10, 2, true, 0.0f),
                FieldDef.createStringField("fchar_5", ColumnType.CHAR, 5, true, "def"),
                FieldDef.createStringField("fnchar_10", ColumnType.NCHAR, 10, true, "def"),
                FieldDef.createStringField("fvarchar_5124", ColumnType.VARCHAR, 5124, true, "default string"),
                FieldDef.createStringField("fnvarchar_155", ColumnType.NVARCHAR, 155, true, "default string"),
                FieldDef.createScalarField("fblob", ColumnType.BLOB, true),
                FieldDef.createScalarField("fbigint", ColumnType.LONG, true),
                FieldDef.createScalarField("fdouble", ColumnType.DOUBLE, true)                
        };
        
        String tabName = "TestPlayer.testDatabaseTypes";
        TableDef td = new TableDef(tabName);
        ArrayList<TableUpdateOperation> nodes = new ArrayList<TableUpdateOperation>();
        int version = 1;
        for (FieldDef fDef : fields) {
            nodes.add(new AddColumnOperation(fDef));
            td.addField(version++, fDef);
        }
        
//        ArrayList<DBOperation> cl = new ArrayList<DBOperation>();
//        cl.add(new DBOperation(tabName, new CreateTableOperation(nodes)));
//        m_player.run(cl);
        
        DBTables ts = new DBTables();
        ts.add(td);
        ts.setActualState(m_player.getActualState());
        m_player.run(ts.getCommandList());

        getConnection().commit();
        
        String dbName = m_player.getActualState().getDBName(tabName);
        
        assertNotNull(dbName);
        
        long tst = (System.currentTimeMillis()/1000)*1000; // Store only seconds        
        
        PreparedStatement ps = getConnection().prepareStatement("insert into "+dbName+" " +
                "(fINT, fSMALLINT, fFLOAT, fTIMESTAMP, fDECIMAL_10_2, fCHAR_5, fNCHAR_10," +
                "fVARCHAR_5124, fNVARCHAR_155, fBLOB, fBIGINT, fDOUBLE) values(?,?,?,?,?,?,?,?,?,?,?,?)");

        Object vals[] = new Object[]{Integer.valueOf(12), Short.valueOf((short) 45), Float.valueOf(43.2f), 
                new Timestamp(System.currentTimeMillis()), BigDecimal.valueOf(62138958, 2), "12345", "hello___90",
                "my mystery", UNICODE_PATTERN, new ByteArrayInputStream("Blob string".getBytes("UTF-8")), 
                Long.valueOf(625395L), Double.valueOf(43.2)};
        
        for (int i = 0; i < vals.length; i++) {
//            if(i==3)
//                ps.setTimestamp(i+1, (Timestamp) vals[i]);
            if(i==9)
                ps.setBinaryStream(i+1, (InputStream) vals[i],"Blob string".length());
            else if(i==4)
                ps.setBigDecimal(i+1, (BigDecimal) vals[i]);
            else if (i == 8){
            	String v = (String) vals[i]; 
            	ps.setString(i+1, v);
            }
            else                
                ps.setObject(i+1, vals[i], FieldDef.sqlType(fields[i].getType()));
        }
        ps.execute();
        getConnection().commit();
        
        
        ps = getConnection().prepareStatement("select * from "+dbName + " where fTIMESTAMP >= ?");
        ps.setTimestamp(1, new Timestamp(tst));
        
        ResultSet rs = ps.executeQuery();
        
        assertTrue(rs.next());
        for (int i = 0; i < vals.length; i++) {
            char[] buf;
            switch(i){
            case 0:
                assertEquals(vals[i], rs.getInt(i+1));
                break;
            case 1:
                assertEquals(vals[i], rs.getShort(i+1));
                break;
            case 2:
                assertEquals(vals[i], rs.getFloat(i+1));
                break;
            case 3:
                //mysql not store milliseconds in timestamp field
                // so check without
                long isTime = rs.getTimestamp(i+1).getTime();
                long hiTime = ((Timestamp)vals[i]).getTime();
                long lowTime = (hiTime/1000)*1000;
                hiTime = lowTime + 1000;
                assertTrue(MessageFormat.format("time {0} must be [{1}, {2}]", isTime, lowTime, hiTime), lowTime <= isTime && isTime <= hiTime);
                break;
            case 4:
                assertEquals(vals[i], rs.getBigDecimal(i+1));
                break;
            case 5:
                assertEquals(vals[i], rs.getString(i+1));
                break;
            case 6:
                assertTrue(rs.getString(i+1), rs.getString(i+1).startsWith((String) vals[i]));
                break;
            case 7:
                assertEquals(vals[i], rs.getString(i+1));
                break;
            case 8:
                assertEquals(vals[i], rs.getString(i+1));
                break;
            case 9:
                InputStream is = rs.getBinaryStream(i+1);
                InputStreamReader reader = new InputStreamReader(is, "UTF-8");
                buf = new char[64];
                int nRead = reader.read(buf);
                String v = new String(buf,0, nRead);
                assertEquals("Blob string", v);
                break;
            case 10:
                assertEquals(vals[i], rs.getLong(i+1));
                break;
            case 11:
                assertEquals(vals[i], rs.getDouble(i+1));
                break;
            }
        }
        rs.close();
        ps.close();
        
    }
    
    public void testSupportForeignKey() throws Exception{
    	{
        	TableDef def1 = new TableDef("testSupportFK_tab1");
        	def1.addScalarField(1, "id", ColumnType.INT, true, null);
        	def1.addPrimaryKey(2, "pk", new String[]{"id"});
        	
        	TableDef def2 = new TableDef("testSupportFK_tab2");
        	def2.addScalarField(1, "id", ColumnType.INT, true, null);
        	def2.addForeignKey(2, "fk", new String[]{"id"}, "testSupportFK_tab1", "pk", false);
        	def2.setSupportForeignKeys(false);

        	TableDef def3 = new TableDef("testSupportFK_tab3");
        	def3.addScalarField(1, "id", ColumnType.INT, true, null);
        	def3.addScalarField(2, "id2", ColumnType.INT, true, null);
           	def3.addPrimaryKey(3, "pk", new String[]{"id"});
        	def3.addForeignKey(4, "fk", new String[]{"id"}, "testSupportFK_tab3", "pk", false);
        	def3.setSupportForeignKeys(false);
        	
        	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
       		desired.put(def1.getEntityName(), def1);
       		desired.put(def2.getEntityName(), def2);
       		desired.put(def3.getEntityName(), def3);
           	DBTables db = new DBTables(m_as, desired);
       		List<DBOperation> cl = db.getCommandList();
        	m_player.run(cl);
        	
        	Connection con = getConnection();
        	DatabaseMetaData md = con.getMetaData();
        	
        	String dbname = m_as.getDBName("testSupportFK_tab1");
        	ResultSet rs = md.getExportedKeys(null, null, dbname);
        	assertFalse(rs.next());
        	
        	dbname = m_as.getDBName("testSupportFK_tab3");
        	rs = md.getExportedKeys(null, null, dbname);
        	assertFalse(rs.next());
    	}
    }
    
    public void testLongConstraintNames() throws Exception{
    	if(getDbType().equals(DbType.FIREBIRD)){
	    	
	    	TableDef tabDef = new TableDef("testLongConstraintNames");
	    	tabDef.addScalarField(1, "f123", ColumnType.INT, true, null);
	    	tabDef.addScalarField(2, "f124", ColumnType.INT, true, null);
	    	String fk1 = "testLongConstraintNames______________1245678901234";
	    	String fk2 = "testLongConstraintNames______________1245678901235";
	    	String idx1 = "testLongConstraintNames______________1245678901236";
	    	String idx2 = "testLongConstraintNames______________1245678901237";
	    	
	    	//-----------------------12345678901234567890123456789012345678901234567890
	    	tabDef.addForeignKey(3, fk1, 
	    			new String[]{"f123"}, TAB_WITH_PK, "pk1", false);
	    	tabDef.addForeignKey(4, fk2, 
	    			new String[]{"f123"}, TAB_WITH_PK, "pk1", false);
	    	tabDef.addIndex(5, idx1, new String[]{"f123"}, false, true);
	    	tabDef.addIndex(6, idx2, new String[]{"f123"}, true, true);
	    	
	    	DBTables db = new DBTables();
	    	db.add(m_tab1);
	    	db.setActualState(m_as);
	    	db.add(tabDef);
	    	m_player.run(db.getCommandList());
	    	
	    	String dbTabName = m_as.getDBName(tabDef.getEntityName());
	    	
	    	String appName1 = m_player.getFullIndexName(
	    			dbTabName, 
					fk1, 
					IndexDef.TYPE.FOREIGN_KEY);
	    	String appName2 = m_player.getFullIndexName(
	    			dbTabName, 
					fk2, 
					IndexDef.TYPE.FOREIGN_KEY);
	    	String appName3 = m_player.getFullIndexName(
	    			dbTabName, 
					idx1, 
					IndexDef.TYPE.INDEX);
	    	
	    	getConnection().commit();
	    	
	    	assertEquals("T_TESTLONGCONSTRAINTNAMES_TESTL", m_as.getIndexDbName(appName1));
	    	
	    	tabDef.deleteIndex(7, fk1);
	    	tabDef.deleteIndex(8, idx1);
	    	m_player.run(db.getCommandList());

	    	assertEquals(appName1, m_as.getIndexDbName( appName1)); // dbIndexName not found 
	    	assertEquals("T_TESTLONGCONSTRAINTNAMES_TEST1", m_as.getIndexDbName(appName2));
	    	assertEquals(appName3, m_as.getIndexDbName(appName3)); // dbIndexName not found 

	    	tabDef.deleteIndex(9, idx2);
	    	m_player.run(db.getCommandList());
    	}
    }
    	
	public void testUnQuotedTable() throws Exception{
		Connection con = getConnection();
		Statement st = con.createStatement();
		
		String dbname = "testUnQuotedTable" + (new Random().nextInt(1000));
		st.execute("create table "+dbname+" (f1 integer, f2 varchar(10))");
		
		con.commit();
		try{
			
			PreparedStatement ps = con.prepareStatement(
					new QInsert(new QTable(dbname).addCol("f1").addCol("f2")).toString());
			
			ps.setInt(1, 123);
			ps.setString(2, "str111");
			ps.addBatch();
			ps.setInt(1, 125);
			ps.setString(2, "str112");
			ps.addBatch();
			ps.executeBatch();
			
			ps = con.prepareStatement(new QSelect().joinLast(new QTable(dbname).addCol("f2"))
					.where(new WhereClause().andCompare("", "f1", "=")).toString());
			ps.setInt(1, 125);
			ResultSet rs = ps.executeQuery();
			assertTrue(rs.next());
			assertEquals("str112", rs.getString(1));
			rs.close();
		}finally{
			st.execute("drop table "+dbname);
			con.commit();
		}
		
	}
	
	public void testSupportNotNullableFields() throws Exception{
    	TableDef def1 = new TableDef("testSupportNNF_tab1");
    	def1.addScalarField(1, "id", ColumnType.INT, true, null);
    	def1.addPrimaryKey(2, "pk", new String[]{"id"});
    	def1.addScalarField(3, "id2", ColumnType.INT, true, null);
    	def1.setSupportNotNullableFields(false);
    	
    	TableDef def2 = new TableDef("testSupportNNF_tab2");
    	def2.addScalarField(1, "id", ColumnType.INT, true, null);
    	def2.addPrimaryKey(2, "pk", new String[]{"id"});
    	def2.addScalarField(3, "id2", ColumnType.INT, true, null);
    	
    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
   		desired.put(def1.getEntityName(), def1);
   		desired.put(def2.getEntityName(), def2);
       	DBTables db = new DBTables(m_as, desired);
   		List<DBOperation> cl = db.getCommandList();
    	m_player.run(cl);
    	
    	
    	assertEquals(false, isNullable("testSupportNNF_tab1", "id"));
    	assertEquals(true,  isNullable("testSupportNNF_tab1", "id2"));
    	assertEquals(false, isNullable("testSupportNNF_tab2", "id"));
    	assertEquals(false, isNullable("testSupportNNF_tab2", "id2"));

	}

	private boolean isNullable(String tab, String col) throws Exception {
    	Connection con = getConnection();
    	DatabaseMetaData md = con.getMetaData();
    	
    	ResultSet rs = md.getColumns(null, null, m_as.getDBName(tab).toUpperCase(), col.toUpperCase());
    	assertTrue(tab+"."+col, rs.next());
    	ApiAlgs.getLog(this).trace(rs.getString("COLUMN_NAME"));
    	return rs.getInt("NULLABLE") != DatabaseMetaData.columnNoNulls;
	}
	
	public void testAutoIncField() throws Exception{
		if(getDbType().equals(DbType.FIREBIRD)){
			Connection con = getConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("select * from RDB$GENERATORS where RDB$GENERATOR_NAME = \'GEN_TES_F1\'");
			if(rs.next())
				st.execute("drop generator GEN_TES_F1");
			con.commit();
		}
	    
    	TableDef def1 = new TableDef("testAutoIncField");
    	FieldDef fd = FieldDef.createScalarField("f1", ColumnType.INT, true);
    	fd.setAutoincrement(true);
    	assertTrue(fd.isAutoincrement());
    	def1.addField(1, fd);
    	def1.addStringField(2, "f2", ColumnType.NVARCHAR, 64, false, null);
    	def1.addPrimaryKey(3, "pk", new String[]{"f1"});
    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
   		desired.put(def1.getEntityName(), def1);
       	DBTables db = new DBTables(m_as, desired);
   		List<DBOperation> cl = db.getCommandList();
    	m_player.run(cl);		
    	
    	Connection con = getConnection();
    	
        if( UpgradeRunner.getDbType(con).equals(UpgradeRunner.DbType.HSQL)){
            return;
        }
        if( UpgradeRunner.getDbType(con).equals(UpgradeRunner.DbType.H2)){
            return;
        }        

    	
    	PreparedStatement ps = con.prepareStatement(
    			new QInsert(new QTable(m_as.getDBName("testAutoIncField"))
    			.addCol("f2")).toString());
    	ps.setString(1, "s1");
    	ps.addBatch();
    	ps.setString(1, "s2");
    	ps.addBatch();
    	ps.executeBatch();
    	
    	ps = con.prepareStatement(new QSelect().joinLast(
    			new QTable(m_as.getDBName("testAutoIncField")).addCol("f1"))
    			.where(new WhereClause().andCompare("", "f2", "=")).toString());
    	try{
    		ps.setString(1, "s1");
	    	ResultSet rs = ps.executeQuery();
	    	assertTrue(rs.next());
	    	assertEquals(1, rs.getInt(1));
	    	rs.close();
	    	ps.setString(1, "s2");
	    	rs = ps.executeQuery();
	    	assertTrue(rs.next());
	    	assertEquals(2, rs.getInt(1));
    	} finally{
    		ps.close();
    	}

	}
	
	public void testCreateIndex() throws EReferenceError, SQLException{
    	TableDef def1 = new TableDef("testCreateIndex");
    	def1.setDbName("testCreateIndex");
    	def1.addField(1, FieldDef.createStringField("f1", ColumnType.VARCHAR, 250,  true, null));
    	def1.addIndex(2, "idx1",new String[]{"f1"}, false, true);
    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
   		desired.put(def1.getEntityName(), def1);
       	DBTables db = new DBTables(m_as, desired);
       	db.setMaxIndexSize(64);
    	m_player.run(db.getCommandList());
		
	}
	
	public void testDropIndex() throws Exception{
		{ // unique index
	    	TableDef def1 = new TableDef("testDropUniqueIndex");
	    	def1.setDbName("testDropUniqueIndex");
	    	def1.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
	    	def1.addField(2, FieldDef.createScalarField("f2", ColumnType.INT, true));
	    	def1.addIndex(3, "unique_idx1",new String[]{"f1"}, true, true);
	    	def1.addIndex(4, "unique_idx2",new String[]{"f2"}, true, true);
	    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
	   		desired.put(def1.getEntityName(), def1);
	       	DBTables db = new DBTables(m_as, desired);
	    	m_player.run(db.getCommandList());
	    	
	    	def1.deleteIndex(5, "unique_idx1");
	    	m_player.run(db.getCommandList());
	    	
	    	Connection con = getConnection();
	    	Statement st = con.createStatement();
	    	st.execute("insert into testDropUniqueIndex (F1, F2) values (50, 50)");
	    	
	    	try{
	    		// unique index constraint works
	        	st.execute("insert into testDropUniqueIndex (F1, F2) values (51, 50)");
	    		fail();
	    	}catch(SQLException e){
	    	}
	    	
	    	// unique index constrain dropped
	    	st.execute("insert into testDropUniqueIndex (F1, F2) values (50, 51)");
		}
		{
	    	TableDef def1 = new TableDef("testDropNonUniqueIndex");
	    	def1.setDbName("testDropNonUniqueIndex");
	    	def1.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
	    	def1.addField(2, FieldDef.createScalarField("f2", ColumnType.INT, true));
	    	def1.addIndex(3, "idx1", new String[]{"f1"}, false, true);
	    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
	   		desired.put(def1.getEntityName(), def1);
	       	DBTables db = new DBTables(m_as, desired);
	    	m_player.run(db.getCommandList());
	    	
	    	def1.deleteIndex(4, "idx1");
	    	m_player.run(db.getCommandList());

		}
    	
	}
	
	public void testAlterColumn() throws EReferenceError, SQLException, InvalidPropertiesFormatException, FileNotFoundException, IOException{
		if(getDbType().equals(DbType.FIREBIRD)){
			return;
		}
    	TableDef def1 = new TableDef("testAlterColumn");
    	def1.setDbName("testAlterColumn");
    	FieldDef oldF = FieldDef.createStringField("f1", ColumnType.NVARCHAR, 60, false, null);
    	def1.addField(1, oldF);
    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
   		desired.put(def1.getEntityName(), def1);
       	DBTables db = new DBTables(m_as, desired);
    	m_player.run(db.getCommandList());
    	
    	FieldDef newF = FieldDef.createStringField("f1", ColumnType.VARCHAR, 60, false, null);
    	def1.addModification(2, new AlterColumnOperation(oldF, newF));
    	m_player.run(db.getCommandList());
    	
    	assertEquals(ColumnType.VARCHAR , def1.getFields().findElement("f1").getElement().getType());
    	
    	oldF = newF;
    	newF = FieldDef.createStringField("f1", ColumnType.VARCHAR, 60, true, null);
    	def1.addModification(3, new AlterColumnOperation(oldF, newF));
    	m_player.run(db.getCommandList());
    	assertEquals(true, def1.getFields().findElement("f1").getElement().bNotNull());
		
	}
	
	public void testClusteredIndex() throws Exception{
		if(UpgradeRunner.getDbType(getConnection()).equals(DbType.MSSQL)){
	    	TableDef def1 = new TableDef("testClusteredIndex");
	    	def1.setDbName("testClusteredIndex");
	    	FieldDef f = FieldDef.createStringField("f1", ColumnType.NVARCHAR, 60, false, null);
	    	def1.addField(1, f);
	    	def1.addIndex(2, "idx",new String[]{"f1"}, false, true, true);
	    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
	   		desired.put(def1.getEntityName(), def1);
	       	DBTables db = new DBTables(m_as, desired);
	    	m_player.run(db.getCommandList());
	    		
		}
	}
	
	public void testEmptyCommand() throws EReferenceError, SQLException{
		{
	    	TableDef def1 = new TableDef("testEmptyCommand");
	    	def1.setDbName("testEmptyCommand");
	    	def1.addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
	    	def1.addModification(2, new EmptyCommand());
	    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
	   		desired.put(def1.getEntityName(), def1);
	       	DBTables db = new DBTables(m_as, desired);
	    	m_player.run(db.getCommandList());
	
	    	assertEquals(2, m_player.getActualState().getVersion("testEmptyCommand"));
		}
		
		{
	    	TableDef def1 = new TableDef("testEmptyCommand_2");
	    	def1.setDbName("testEmptyCommand_2");
	    	def1.addField(1, FieldDef.createStringField("f1", ColumnType.CHAR, 40, true, null));
	    	def1.addPrimaryKey(2, "pk", new String[]{"f1"});
	    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
	   		desired.put(def1.getEntityName(), def1);
	       	DBTables db = new DBTables(m_as, desired);
	       	db.setMaxIndexSize(20);
	    	m_player.run(db.getCommandList());

	    	assertEquals(2, m_player.getActualState().getVersion("testEmptyCommand"));

		}
	}
	
	public void testRollbackError() throws Exception{
		Connection con = getConnection();
		con.createStatement().execute("create table testRollbackError_exist (F0 INT)");
		try{
			
	    	TableDef def1 = new TableDef("testRollbackError");
	    	def1.setDbName("testRollbackError");
	    	FieldDef f = FieldDef.createStringField("f1", ColumnType.NVARCHAR, 60, true, null);
	    	def1.addField(1, f);
	    	HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
	   		desired.put(def1.getEntityName(), def1);
	       	DBTables db = new DBTables(m_as, desired);
	    	m_player.run(db.getCommandList());
	
	    	def1.addField(2, FieldDef.createStringField("f2", ColumnType.NVARCHAR, 60, false, null)); // Field should be rollbacked
	    	def1.addPrimaryKey(3, "pk", new String[]{"f1"});
	    	TableDef def2 = new TableDef("testRollbackError_exist");
	    	def2.setDbName("testRollbackError_exist"); // Table with this name already exists in base
	    	def2.addField(1, FieldDef.createStringField("f1", ColumnType.NVARCHAR, 60, false, null));
	    	def2.addForeignKey(2, "fk", new String[]{"f1"}, "testRollbackError", "pk", false);

	    	desired.put(def2.getEntityName(), def2);
	       	db = new DBTables(m_as, desired);
	       	try{
	       		m_player.run(db.getCommandList());
	       		fail();
	       	}catch (SQLException  e) {}
	    	
	    	try{
	    		con.createStatement().execute("select f2 from testRollbackError"); // check F2 rollbacked
	    		fail();
	    	}catch(SQLException e){}
	    	
		} finally{
			con.createStatement().execute("drop table testRollbackError_exist");
			con.commit();
		}
	}

	public void testRunOperation()
	 throws Exception {
	
	}
    
	static class TestDropCoumn2 extends TableDef{
		public TestDropCoumn2() {
			super();
			addField(1, FieldDef.createScalarField("f1", ColumnType.INT, true));
			addField(2, FieldDef.createScalarField("f2", ColumnType.INT, true));
			addPrimaryKey(3, "pk", new String[]{"f1"});
			deleteIndex(4, "pk");
			deleteField(5, "f1");
			addField(6, FieldDef.createScalarField("f3", ColumnType.LONG, true));
		}
		
	}
	
	public void testDropCoumn() throws EReferenceError, SQLException{
		HashMap<String, TableDef> desired = new HashMap<String, TableDef>();
		TestDropCoumn2 def1 = new TestDropCoumn2();
   		desired.put(def1.getEntityName(), def1);
       	DBTables db = new DBTables(m_as, desired);
    	m_player.run(db.getCommandList());
    	
	}
}
