/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.upg_procedures;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.triniforce.db.ddl.Delta;
import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.Delta.DBMetadata;
import com.triniforce.db.ddl.Delta.IDBNames;
import com.triniforce.db.ddl.Delta.DBMetadata.IIndexLocNames;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.ddl.UpgradeRunner.IActualState;
import com.triniforce.server.plugins.kernel.BasicServer;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.ISODbInfo;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class ConvertForeignKeys  extends UpgradeProcedure {

    public ConvertForeignKeys() {
        super("Convert foreign keys");
    }

    @Override
    public void run() throws Exception {
        Connection con = SrvApiAlgs2.getPooledConnection();
        try {
            ISODbInfo dbInfo = ApiStack.getInterface(ISODbInfo.class);
            if (isNewDb(con, dbInfo.getTableDbName(BasicServer.DPP_TABLE)))
                return;
            runAlways();
        } finally {
            SrvApiAlgs2.returnPooledConnection(ApiStack.getApi(), con);
        }
    }
    
    public void runAlways() throws Exception {
        Connection con = SrvApiAlgs2.getPooledConnection();
        try {
            ISODbInfo dbInfo = ApiStack.getInterface(ISODbInfo.class);
            IActualState as = ((BasicServer) ApiStack
                    .getInterface(IBasicServer.class)).getActualDbState();
            UpgradeRunner pl = new UpgradeRunner(con, as);
            DBMetadata md = new Delta.DBMetadata(con.getMetaData());
            Statement st = con.createStatement();
            for (String dbName : dbInfo.getDbTableNames()) {
                List<IndexDef> fks = md.getForeignKeys(dbInfo2DbNames(dbInfo),
                        new IIndexLocNames() {
                            public String getShortName(String dbTabName,
                                    String dbFullName) {
                                return dbFullName;
                            }
                        }, dbName);
                for (IndexDef fk : fks) {
                    exec(st, getDelForeignKeyString(pl, dbName, fk.getName()));
                    if(!DbType.MYSQL.equals(UpgradeRunner.getDbType(con)) || 
                            !md.isIndexExists(dbName, fk.getName())){
                        exec(st, getCreateIndexString(pl, dbName, fk.getName(),
                                fk.getColumns()));
                    }
                }

            }
            con.commit();
        } finally {
            SrvApiAlgs2.returnPooledConnection(ApiStack.getApi(), con);
        }
    }

    private void exec(Statement st, String sql) throws SQLException {
        ApiAlgs.getLog(this).trace(sql);
        st.execute(sql);
    }

    private String getCreateIndexString(UpgradeRunner pl, String dbName, 
            String fkName, List<String> cols) {
        return pl.getCreateIndexOperationString(cols, dbName, fkName, false, true);
    }

    private String getDelForeignKeyString(UpgradeRunner pl, String dbName, String fkName) {
        return pl.getDeleteIndexOperationString(
                IndexDef.TYPE.FOREIGN_KEY, dbName, fkName, false);
    }
    private IDBNames dbInfo2DbNames(final ISODbInfo dbInfo) {
        return new IDBNames(){
            public String getAppName(String dbName) {
                return dbInfo.getTableAppName(dbName);
            }
            public String getDbName(String appName) {
                return dbInfo.getTableDbName(appName);
            }
        };
    }
    
    boolean isNewDb(Connection con, String dppTab){
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select count(*) from " + dppTab);
            try{
                ApiAlgs.assertTrue(rs.next(),"");
                return 0 == rs.getInt(1);
            }finally{
                rs.close();
            }
        } catch (SQLException e) {
            ApiAlgs.rethrowException(e);
            return false;
        }        
    }
}
