/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.utils.ApiAlgs;

public class CleanDatabase {

    public static void main(String[] args) throws Exception {

        DBTestCase test = new DBTestCase();
        test.setUp();
        try {

            test.trace("Database clean.");

//            test.getConnection().commit();
            DBTestCase.getDataSource().setMaxActive(0);
            DBTestCase.getDataSource().setMaxIdle(1);
            DBTestCase.getDataSource().setMaxActive(1);
            run(test,  ApiAlgs.getLog(test));

            test.trace("Ok. Done.");
        } finally {
            test.tearDown();
        }

    }

    public static void run(DBTestCase test) throws Exception {
    	test.setUp();
    	try{
//        Connection conn = test.getNewConnection();
//        try {
        	run(test, ApiAlgs.getLog(test));
//        } finally {
//            conn.close();
//        }
    	}finally{
    		test.tearDown();
    	}
    }
    
    public static void run(DBTestCase test, Log log) throws Exception {

    		Connection conn = test.reopenConnection();
	    	String schem = null;
	    	DbType dbType = DBTestCase.getDbType();
	    	boolean autoCommmit = conn.getAutoCommit();
	    	try{
		        if (dbType.equals(DbType.MSSQL)){
		            conn.setAutoCommit(true);
		            schem = "dbo";
		        }
		        else
		            conn.setAutoCommit(false);
		
		        ActualStateBL as = new ActualStateBL(conn);
		        Set<String> dbnames = new HashSet<String>(as.getDbTableNames());
		        dbnames.remove(ActualStateBL.ACT_STATE_TABLE);
	//	        dbnames.remove(as.getDBName(TIndexNames.class.getName()));
		        conn = test.reopenConnection();
		        conn.setAutoCommit(false);
		        DatabaseMetaData md = conn.getMetaData();
	//	        conn.commit();
	//	        ResultSet rs = md.getTables(conn.getCatalog(), schem, "%",
	//	                new String[] { "TABLE", "VIEW" });
		        for (String dbName  : dbnames) {
	//	            String dbName = rs.getString("TABLE_NAME");
	//	            if("VIEW".equals(rs.getString("TABLE_TYPE").toUpperCase())){
	//		            log.trace(String.format("Delete view %s.", dbName));
	//		        	conn.createStatement().execute("DROP VIEW "+dbName);            	
	//	            }
	//	            else{
			            
			
			            try {
			                dropTable(conn, dbName);
				            log.trace(String.format("Delete table %s. COMPLETED", dbName));
			            } catch (SQLException e) {
				            log.trace(String.format("Delete table %s. Excepted, cause: %s", dbName, e.toString()));
			
			                // Search for exported foreign keys on table being deleted
			                ResultSet fkRs = md.getExportedKeys(null, schem, dbName);
			                String oldFkTab = null, oldFkName = null;
			                while (fkRs.next()) {
			                    String fkTab = fkRs.getString("FKTABLE_NAME");
			                    String fkName = fkRs.getString("FK_NAME");
			                    log.trace(String.format("Delete exported key %s.%s.",
			                            fkTab, fkName));
			                    if (!(fkTab.equals(oldFkTab) && fkName
			                            .equals(oldFkName))) {
			                        PreparedStatement ps = null;
			                        if (DBTestCase.getDbType().equals(DbType.MYSQL))
			                            ps = conn
			                                    .prepareStatement("ALTER TABLE "
			                                            + fkTab + " DROP FOREIGN KEY "
			                                            + fkName);
			                        else
			                            ps = conn.prepareStatement("ALTER TABLE "
			                                    + fkTab + " DROP CONSTRAINT " + fkName);
			                        ps.execute();
			                        ps.close();
			                    }
			                    oldFkTab = fkTab;
			                    oldFkName = fkName;
			                }
			                try{
			                	dropTable(conn, dbName);
					            log.trace(String.format("Delete table %s. COMPLETED", dbName));
			                }catch(SQLException e2){
			                	log.trace(String.format("Drop table  FAILED %s.", dbName));
	//		                	log.trace("cause", e2);
			                }
			                catch(Exception e2){
			                	log.trace("unresolved drop : " + dbName);
	//		                	PreparedStatement ps = conn.prepareStatement("select * from MON$ATTACHMENTS");
	//		                	ResultSet rs = ps.executeQuery();
	//		                	while(rs.next()){
	//		                		log.trace("conn: " + rs.getTimestamp("MON$TIMESTAMP"));
	//		                	}
	//		                	throw e2;
			                	
			                }
	//		                as.removeTable(as.getAppName(dbName));
			            }
	//	            }
		        }
	//	        as.flush(conn);
		        dropTable(conn, ActualStateBL.ACT_STATE_TABLE);
		        conn.commit();
	//	        rs.close();
		        // if database is firebird
		        // drop all client generators
//		        if(DbType.FIREBIRD.equals(dbType)){
//		        	Statement st = conn.createStatement();
//		        	ResultSet rs = st.executeQuery("select RDB$GENERATOR_NAME from RDB$GENERATORS where RDB$SYSTEM_FLAG is null");
//		        	while(rs.next()){
//		        		Statement st2 = conn.createStatement();
//		        		st2.execute("DROP GENERATOR "+rs.getString(1));
//		        		st2.close();
//		        	}
//		        	rs.close();
//		        	st.close();
//		        }
		        if (!conn.getAutoCommit())
		            conn.commit();
	    	} finally{
	    		conn.setAutoCommit(autoCommmit);
	    	}
    }

    private static void dropTable(Connection conn, String dbName)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DROP TABLE " + dbName);
        ps.execute();
        ps.close();
        conn.commit();
        
        if(!dbName.equals(ActualStateBL.ACT_STATE_TABLE)){
	        ps = conn.prepareStatement("delete from ACTUAL_TABLE_STATES where DBNAME = \'" + dbName+"\'");
	        ps.execute();
	        ps.close();
	        conn.commit();
        }
    }
    
    
}
