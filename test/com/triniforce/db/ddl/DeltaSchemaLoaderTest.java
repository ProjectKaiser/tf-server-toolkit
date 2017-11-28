/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.triniforce.db.ddl.Delta.DBMetadata.IIndexLocNames;
import com.triniforce.db.ddl.Delta.DeltaSchema;
import com.triniforce.db.ddl.Delta.DeltaSchemaLoader;
import com.triniforce.db.ddl.Delta.EditTabCmd;
import com.triniforce.db.ddl.Delta.IDBNames;
import com.triniforce.db.ddl.TableDef.ElementVerStored;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.test.DBTestCase;
import com.triniforce.utils.ApiAlgs;

public class DeltaSchemaLoaderTest extends DBTestCase {

	private ActualStateBL m_as;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Connection con = getConnection();
		m_as = new ActualStateBL(getConnection());
		
        DBTables ts = new DBTables();
        ts.setActualState(m_as);

		
		TableDef tabDef = new TableDef("DeltaSchemaLoaderTest1");
		tabDef.addStringField(1, "field1", ColumnType.NVARCHAR, 100, false, null);
		tabDef.addScalarField(2, "field2", ColumnType.INT, true, 999);
		tabDef.addDecimalField(3, "field3", 12, 4, true,null);
		tabDef.addScalarField(4, "field4", ColumnType.INT, false, null);
		tabDef.addPrimaryKey(5, "testPK", new String[]{"field2", "field3"});
		tabDef.addIndex(6, "idx_01", new String[]{"field4"}, false, true);
		tabDef.addIndex(7, "idx_02", new String[]{"field3"}, true, false);
		ts.add(tabDef);
        //createTableIfNeeded(tabDef, m_as);

		tabDef = new TableDef("DeltaSchemaLoaderTest2");
		tabDef.setSupportForeignKeys(true);
		tabDef.addStringField(1, "field1", ColumnType.NVARCHAR, 100, false, null);
		tabDef.addStringField(2, "field2", ColumnType.CHAR, 15, true, "123456789012345");
		tabDef.addStringField(3, "field3", ColumnType.NCHAR, 15, true, "123456789012345");
		tabDef.addStringField(4, "field4", ColumnType.VARCHAR, 15, true, "hihihihih");
		tabDef.addDecimalField(5, "field5", 12, 4, true, null);
		tabDef.addForeignKey(6, "testFK", new String[]{"field5"}, "DeltaSchemaLoaderTest1", "idx_02", false);
		ts.add(tabDef);
        //createTableIfNeeded(tabDef, m_as);
		
		tabDef = new TableDef("DeltaSchemaLoaderTest3");
		tabDef.addStringField(1, "field1", ColumnType.NVARCHAR, 100, false, null);
		tabDef.addModification(2, new AddIndexOperation(IndexDef.createIndex("DeltaSchemaLoaderTest3_ORIGINAL_DB_NAME", 
				Arrays.asList("field1"), false, true, false, null, null, true)));
		ts.add(tabDef);

		
        UpgradeRunner pl = new UpgradeRunner(getConnection(), m_as);
        pl.run(ts.getCommandList());
        con.commit();
	}
	
	public void test() throws Exception{
//		if(!(getDbType().equals(DbType.MSSQL) || getDbType().equals(DbType.FIREBIRD)))
//			return;
		
		DeltaSchemaLoader loader = new Delta.DeltaSchemaLoader(
				Arrays.asList("DeltaSchemaLoaderTest1","DeltaSchemaLoaderTest2", "UnkTable1","DeltaSchemaLoaderTest3"),
				new IIndexLocNames(){
					public String getShortName(String dbTabName,
							String dbFullName) {
						if(dbFullName.startsWith(dbTabName.toUpperCase()+"_")){
							return dbFullName.substring(dbTabName.length()+1);
						}
						return dbFullName;
					}
					
				});
		//DeltaSchema sch = loader.loadSchema(getConnection());
		//assertNotNull(sch);
		
		//Map<String, DeltaTable> tabs = sch.getTables();
		
		//assertTrue(tabs.keySet().toString(), tabs.containsKey(m_as.getDBName("DeltaSchemaLoaderTest1").toUpperCase()));
		//assertTrue(tabs.keySet().toString(), tabs.containsKey(m_as.getDBName("DeltaSchemaLoaderTest2").toUpperCase()));
		
		DeltaSchema sch = loader.loadSchema(getConnection(), dbInfoFromAS(m_as));
		Map<String, TableDef> tabs = sch.getTables();
		assertTrue(tabs.keySet().toString(), tabs.containsKey("DeltaSchemaLoaderTest1"));
		assertFalse(tabs.keySet().toString(), tabs.containsKey(ActualStateBL.ACT_STATE_TABLE));
		
		TableDef tab = tabs.get("DeltaSchemaLoaderTest1");
		FieldDef field = tab.getFields().findElement("FIELD1").getElement();
		assertNotNull(field);
		if(getDbType().equals(DbType.MSSQL))
			assertEquals(ColumnType.NVARCHAR, field.getType());
		assertEquals(100, field.getSize());
		
		field = tab.getFields().findElement("FIELD2").getElement();
		assertEquals(ColumnType.INT, field.getType());
		field = tab.getFields().findElement("FIELD3").getElement();
		assertEquals(ColumnType.DECIMAL, field.getType());
		assertEquals(12, field.getSize());
		assertEquals(4, field.getScale());
		
		Map<String, IndexDef> indices = EditTabCmd.toMap(tab.getIndices());
		String pkName = getDbType().equals(DbType.MYSQL) ? "PRIMARY" : "TESTPK";
		IndexDef index = indices.get(pkName);
		assertNotNull(indices.keySet().toString(), index);
		assertEquals(2, index.m_columns.size());
		assertTrue(index.m_columns.toString(), index.m_columns.containsAll(Arrays.asList("FIELD2","FIELD3")));
		assertEquals(IndexDef.TYPE.PRIMARY_KEY, index.getType());
		
		index = indices.get("IDX_01");
		assertNotNull(indices.keySet().toString(), index);
		assertTrue(index.m_columns.toString(), index.m_columns.containsAll(Arrays.asList("FIELD4")));
		assertEquals(IndexDef.TYPE.INDEX, index.getType());
		assertFalse(index.m_bUnique);
		assertTrue(index.m_bAscending);
		
		index = getIndexOn(indices.values(), Arrays.asList("FIELD3"));
		assertNotNull(index);
//		if(!getDbType().equals(DbType.DERBY))
//			assertEquals("IDX_02", index.getName());
		//index = indices.get());
		assertNotNull(indices.keySet().toString(), index);
		assertTrue(index.m_columns.toString(), index.m_columns.containsAll(Arrays.asList("FIELD3")));
		assertEquals(IndexDef.TYPE.INDEX, index.getType());
		assertTrue(index.m_bUnique);
		//if(!getDbType().equals(DbType.FIREBIRD))
		//	assertFalse(index.m_bAscending);
		
		
		tab = tabs.get("DeltaSchemaLoaderTest2");
		Map<String, FieldDef> fields = EditTabCmd.toMap(tab.getFields());
		field = fields.get("FIELD1");
		assertNotNull(fields.keySet().toString(), field);
		assertFalse(field.bNotNull());
		field = fields.get("FIELD2");
		if(!DbType.DERBY.equals(getDbType()))
			assertEquals(ColumnType.CHAR, field.getType());
		//assertEquals("\'123456789012345\'", field.getDefaultValue());
		assertTrue(field.bNotNull());
		
		
		field = tab.getFields().findElement("FIELD4").getElement();
		if(!DbType.DERBY.equals(getDbType()))
			assertEquals(ColumnType.VARCHAR, field.getType());
		assertEquals(15, field.getSize());
		//assertEquals("\'hihihihih\'", field.getDefaultValue());
		
		
		index = tab.getIndices().findElement(m_as.getIndexDbName( 
				"TESTFK")).getElement();
		assertNotNull(index);
		assertEquals(IndexDef.TYPE.FOREIGN_KEY, index.getType());
		assertEquals(Arrays.asList("FIELD5"), index.getColumns());
		assertEquals("DeltaSchemaLoaderTest1", index.getParentTable());
//		if(!getDbType().equals(DbType.MYSQL))
//			assertEquals("IDX_02", index.getParentIndex());
		
		
		
		{// test index db name
			DeltaSchemaLoader loader2 = new Delta.DeltaSchemaLoader(
					Arrays.asList("DeltaSchemaLoaderTest3"),
					new DeltaSchemaLoader.OlapIndexLocNames());
			DeltaSchema tabs2 = loader2.loadSchema(getConnection(), dbInfoFromAS(m_as));
			tab = tabs2.getTables().get("DeltaSchemaLoaderTest3");
			assertNotNull(tab);
			IndexDef idx0 = tab.getIndices().getElement(0);
			assertEquals("DeltaSchemaLoaderTest3_ORIGINAL_DB_NAME", idx0.getName());
			ElementVerStored<IndexDef> res = tab.getIndices().findElement("DeltaSchemaLoaderTest3_ORIGINAL_DB_NAME");
			assertNotNull(res);
			assertTrue(idx0.isOriginalDbName());
		}
	}

	
	private IDBNames dbInfoFromAS(final ActualStateBL as) {
		return new Delta.IDBNames(){

			public String getAppName(String dbName) {
				try {
					return as.getAppName(dbName);
				} catch (SQLException e) {
					ApiAlgs.rethrowException(e);
					return null;
				}
			}

			public String getDbName(String appName) {
				return as.getDBName(appName);
			}
			
		};
	}

	private IndexDef getIndexOn(Collection<IndexDef> indices, List<String> cols) {
		for (IndexDef index : indices) {
			if(index.getColumns().size() == cols.size() &&
					index.getColumns().containsAll(cols))
				return index;
		}
		return null;
	}
	
//	public void testExclusion() throws Exception{
//		DeltaSchemaLoader loader = new Delta.DeltaSchemaLoader(
//				Arrays.asList("DeltaSchemaLoaderTest1","DeltaSchemaLoaderTest2", "UnkTable1"));
//		loader.addExclusion("DeltaSchemaLoaderTest2", "FIELD3");
//		loader.addExclusion("DeltaSchemaLoaderTest1", "FIELD4");
//		
//		DeltaSchema sch = loader.loadSchema(getConnection(), dbInfoFromAS(m_as));
//		Map<String, TableDef> tabs = sch.getTables();
//		
//		assertNull(tabs.get("DeltaSchemaLoaderTest2").getFields().findElement("FIELD3"));
//		assertNull(tabs.get("DeltaSchemaLoaderTest1").getFields().findElement("FIELD4"));
//	}
	
	public void testLONGVARBINARY() throws SQLException, Exception{
		if(getDbType().equals(DbType.FIREBIRD)){
			Connection con = getConnection();
			Statement st = con.createStatement();
			if(!existTable("TEST_LONGVARBINARY")){
				st.execute("CREATE TABLE TEST_LONGVARBINARY (DESCRIPTION BLOB SUB_TYPE 1 SEGMENT SIZE 4096)");
				con.commit();
			}
			try{
				PreparedStatement ps = con.prepareStatement("insert into TEST_LONGVARBINARY(DESCRIPTION) VALUES(?)");
				ps.setObject(1, "TESTESTSETES".getBytes());
				ps.execute();
				
				DeltaSchemaLoader loader = new DeltaSchemaLoader(Arrays.asList("TEST_LONGVARBINARY"), null);
				DeltaSchema res = loader.loadSchema(con, new IDBNames() {
					public String getDbName(String appName) {
						return appName;
					}
					
					public String getAppName(String dbName) {
						return dbName;
					}
				});
				
				//loaded
				assertEquals(FieldDef.ColumnType.BLOB, 
						res.getTables().get("TEST_LONGVARBINARY").getFields().findElement("DESCRIPTION").getElement().getType());
				
				ps.close();
				
				ps  = con.prepareStatement("select DESCRIPTION FROM TEST_LONGVARBINARY");
				ResultSet rs = ps.executeQuery();
				rs.next();
				Blob b = rs.getBlob(1);
				assertNotNull(b);
				
				byte[] bytes = rs.getBytes(1);
				assertEquals("TESTESTSETES", new String(bytes, "utf-8"));
				
				char[] cbuf = new char[100];
				
				InputStream in = rs.getBinaryStream(1);
				InputStreamReader r = new InputStreamReader(in, "utf-8");
				int nr = r.read(cbuf);
				assertEquals("TESTESTSETES", new String (cbuf, 0, nr));

				rs.close();
				ps.close();
						
			}finally{
//				st.execute("drop table TEST_LONGVARBINARY");
//				con.commit();
			}
		}
	}

private boolean existTable(String tabName) throws SQLException, Exception {
	ResultSet rs = getConnection().getMetaData().getTables(null, null, tabName.toUpperCase(), null);
	try{
		return rs.next();
	}finally{
		rs.close();
	}
}
	
}
