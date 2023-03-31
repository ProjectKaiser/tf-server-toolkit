/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.Delta;
import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.Delta.DeltaSchema;
import com.triniforce.db.ddl.Delta.DeltaSchemaLoader;
import com.triniforce.db.ddl.Delta.DBMetadata.IIndexLocNames;
import com.triniforce.db.qbuilder.Expr;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QTable;
import com.triniforce.db.qbuilder.WhereClause;
import com.triniforce.db.test.DBTestCase;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.server.srvapi.ISOQuery.EServerObjectNotFound;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class DBCopyTest extends TFTestCase {
	
	@Override
	protected void setUp() throws Exception {
        Api api = new Api();
        api.setIntfImplementor(IDatabaseInfo.class, DBTestCase.getDbInfo());
        ApiStack.pushApi(api);
        super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ApiStack.popApi();
	}

	@Override
	public void test() throws Exception {
		DeltaSchemaLoader loader;
		DeltaSchema dst;
		{
			Class.forName("org.firebirdsql.jdbc.FBDriver");
			String dstUrl = "jdbc:firebirdsql:localhost/3050:C:/work/regression.gdb";
			Connection dstCon;
	        String dbUserName = getTestProperty("userName");
	        if(null != dbUserName){
		        String dbPassword = getTestProperty("password");
				dstCon = DriverManager.getConnection(dstUrl, dbUserName, dbPassword);
	        }
	        else{
	        	dstCon = DriverManager.getConnection(dstUrl);
	        }
			try{
				loader = new Delta.DeltaSchemaLoader(getTabList(dstCon), new Delta.DeltaSchemaLoader.TemplateIndexLocNames(UpgradeRunner.getDbType(dstCon)));
//				loader = new Delta.DeltaSchemaLoader(Arrays.asList("purchase_order"));
				dst = loader.loadSchema(dstCon, getDstDbInfo());
			} finally{
				dstCon.close();
			}
		}
		
		DeltaSchema src;
		{
			Connection srcCon = getDataSource().getConnection();
	        try{
				ActualStateBL as = new ActualStateBL(srcCon);
	        	src = loader.loadSchema(srcCon, dbInfoFromAS(as));
				Delta delta = new Delta();
				List<DBOperation> ops = delta.calculateDelta(src.getTables(), dst.getTables());
				UpgradeRunner runner = new UpgradeRunner(srcCon, as);
				runner.run(ops);
	        }finally{
	        	srcCon.close();
	        }
		}
		
		
	}
	
	private Delta.IDBNames dbInfoFromAS(final ActualStateBL as) {
		return new Delta.IDBNames(){

			public String getDbName(String entityName) throws EServerObjectNotFound {
				return as.getDBName(entityName);
			}

			public String getAppName(String dbName) {
				try {
					return as.getAppName(dbName);
				} catch (SQLException e) {
					ApiAlgs.rethrowException(e);
					return null;
				}
			}
			
		};
	}

	private Delta.IDBNames getDstDbInfo() {
		return new Delta.IDBNames(){

			public String getDbName(String entityName) throws EServerObjectNotFound {
				return entityName.substring(entityName.lastIndexOf(".")+1);
			}

			public String getAppName(String dbName) {
				return PKG+"."+dbName;
			}
			
		};
	}
	
	

	static String PKG = DBCopyTest.class.getPackage().getName();
	
	

	private List<String> getTabList(Connection dstCon) throws SQLException {
		ResultSet rs = dstCon.createStatement().executeQuery(
				new QSelect().joinLast(new QTable("v_st").addCol("table_name"))
				.where(new WhereClause().and(new Expr.Compare("", "log", "=", "\'b\'"))).toString());
		
		ArrayList<String> res = new ArrayList<String>();
		while(rs.next())
			res.add(PKG+"."+rs.getString(1));
		rs.close();
		return res;
	}
	
	public void testFbTypes() throws ClassNotFoundException, SQLException {
		DeltaSchemaLoader loader;
		DeltaSchema dst;
		{
			Class.forName("org.firebirdsql.jdbc.FBDriver");
			//String dstUrl = "jdbc:firebirdsql:localhost/3050:D:\\workspace\\UBL\\build\\test\\regression.fdb";
			String dstUrl = "jdbc:firebirdsql://localhost/D:/workspace/tf_settings/regression.fdb?encoding=UNICODE_FSS";
			Connection dstCon;
	        String dbUserName = getTestProperty("userName");
	        if(null != dbUserName){
		        String dbPassword = getTestProperty("password");
				dstCon = DriverManager.getConnection(dstUrl, dbUserName, dbPassword);
	        }
	        else{
	        	dstCon = DriverManager.getConnection(dstUrl);
	        }
			try{
				loader = new Delta.DeltaSchemaLoader(getTabList(dstCon), 
						new Delta.DeltaSchemaLoader.TemplateIndexLocNames(UpgradeRunner.getDbType(dstCon)));
//				loader = new Delta.DeltaSchemaLoader(Arrays.asList("purchase_order"));
				dst = loader.loadSchema(dstCon, getDstDbInfo());
			} finally{
				dstCon.close();
			}
		}
			
	}
	
}
