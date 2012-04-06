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
import java.sql.Statement;

import org.apache.commons.logging.Log;

import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.utils.ApiAlgs;

public class CleanDatabase {

    public static void main(String[] args) throws Exception {

        DBTestCase test = new DBTestCase();
        test.setUp();
        try {

            test.trace("Database clean.");

            run(test);

            test.trace("Ok. Done.");
        } finally {
            test.tearDown();
        }

    }

    public static void run(DBTestCase test) throws Exception {
        Connection conn = test.getNewConnection();
        try {
        	run(conn, ApiAlgs.getLog(test));
        } finally {
            conn.close();
        }
    }
    
    public static void run(Connection conn, Log log) throws Exception {
    	
    	String schem = null;
    	DbType dbType = DBTestCase.getDbType();
        if (dbType.equals(DbType.MSSQL)){
            conn.setAutoCommit(true);
            schem = "dbo";
        }
        else
            conn.setAutoCommit(false);
        DatabaseMetaData md = conn.getMetaData();

        ResultSet rs = md.getTables(conn.getCatalog(), schem, "%",
                new String[] { "TABLE", "VIEW" });
        while (rs.next()) {
            String dbName = rs.getString("TABLE_NAME");
            if("VIEW".equals(rs.getString("TABLE_TYPE").toUpperCase())){
	            log.trace(String.format("Delete view %s.", dbName));
	        	conn.createStatement().execute("DROP VIEW "+dbName);            	
            }
            else{
	            
	            log.trace(String.format("Delete table %s.", dbName));
	
	            try {
	                dropTable(conn, dbName);
	            } catch (SQLException e) {
	
	                // Search for exported foreign keys on table being deleted
	                ResultSet fkRs = md.getExportedKeys(null, null,dbName);
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
	                dropTable(conn, dbName);
	            }
            }
        }
        rs.close();
        // if database is firebird
        // drop all client generators
        if(DbType.FIREBIRD.equals(dbType)){
        	Statement st = conn.createStatement();
        	rs = st.executeQuery("select RDB$GENERATOR_NAME from RDB$GENERATORS where RDB$SYSTEM_FLAG is null");
        	while(rs.next()){
        		Statement st2 = conn.createStatement();
        		st2.execute("DROP GENERATOR "+rs.getString(1));
        		st2.close();
        	}
        	rs.close();
        	st.close();
        }
        if (!conn.getAutoCommit())
            conn.commit();
    }

    private static void dropTable(Connection conn, String dbName)
            throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DROP TABLE " + dbName);
        ps.execute();
        ps.close();
    }
}
