/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.br;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.export.MappedDataSet;
import com.triniforce.server.plugins.kernel.ep.br.IBackupStorage;
import com.triniforce.server.plugins.kernel.ep.br.IRestoreStorage;
import com.triniforce.server.plugins.kernel.ep.br.PKEPBackupRestoreEntry;
import com.triniforce.server.plugins.kernel.ext.br.BackupRestorePluginVersions.PluginVersions;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.VersionComparator;

/**
 * Tested in BasicServerTest
 */
public class BackupRestoreDb extends PKEPBackupRestoreEntry{
    
    public static final String KEY_DB = "db";
    public static final String FILE_DBDATA = "data.xml";
    public static final String FILE_DTD = "dtd.xml";

    void config(IDatabaseConnection srcConnection){
        //srcConnection.getConnection().e
        DatabaseConfig cfg = srcConnection.getConfig();
        //cfg.setProperty("http://www.dbunit.org/features/qualifiedTableNames", false);
        IDatabaseInfo di = ApiStack.getInterface(IDatabaseInfo.class);
        String pattern = di.getIdentifierQuoteString() + "?" + di.getIdentifierQuoteString();
        cfg.setProperty("http://www.dbunit.org/properties/escapePattern", pattern);
        cfg.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
    }
    
    @Override
    public void backup(IBackupStorage stg){
        IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
        stg.writeObject(KEY_DATA, new PluginVersions(bs));
        IPooledConnection pc = ApiStack.getInterface(IPooledConnection.class);
        try {
            Connection con = pc.getPooledConnection();
            try {
            	String scheme = null;
            	if(DbType.MSSQL.equals(UpgradeRunner.getDbType(con)))
            		scheme = "dbo";
            	if(DbType.H2.equals(UpgradeRunner.getDbType(con)))
            		scheme = "PUBLIC";
                IDatabaseConnection srcConnection = new DatabaseConnection(con, scheme);
                config(srcConnection);
                IDataSet fullDataSet = srcConnection.createDataSet();
                File tmpFolder = stg.getTempFolder();
                FlatXmlDataSet.write(fullDataSet, new FileOutputStream(new File(tmpFolder, FILE_DBDATA)));
                FlatDtdDataSet.write(fullDataSet, new FileOutputStream(new File(tmpFolder, FILE_DTD)));
                stg.writeFolder(KEY_DB, tmpFolder);
            } finally {
                pc.returnConnection(con);
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

    @Override
    public String initRestore(IRestoreStorage stg) {
        PluginVersions archivePV = (PluginVersions) stg.readObject(KEY_DATA);
        IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
        PluginVersions currentPV = new PluginVersions(bs);
        if( VersionComparator.compareVersions(currentPV.getPluginVersions(), archivePV.getPluginVersions()) < 0){
            return "Archive contains newer plugin versions";
        }
        return null;        
    }
    
    @Override
    public void restore(IRestoreStorage stg){
        File tmpFolder = stg.getTempFolder();
        stg.restoreFolder(KEY_DB, tmpFolder);
        File data = new File(tmpFolder, FILE_DBDATA);
        File dtd = new File(tmpFolder, FILE_DTD);
        IPooledConnection pc = ApiStack.getInterface(IPooledConnection.class);
        try {
            Connection con = pc.getPooledConnection();
            try {
            	String scheme = null;
            	if(DbType.MSSQL.equals(UpgradeRunner.getDbType(con)))
            		scheme = "dbo";
                IDatabaseConnection dstConnection = new DatabaseConnection(con, scheme);
                config(dstConnection);
                HashMap<String, String> dbNames = new HashMap<String, String>();
                {
                    Statement st = con.createStatement();
                    ResultSet rs = st
                            .executeQuery("select APPNAME, DBNAME from actual_table_states");
                    while (rs.next()) {
                        dbNames.put(rs.getString(1), rs.getString(2));
                    }
                    rs.close();
                    st.close();
                }
                FlatXmlDataSetBuilder xb = new FlatXmlDataSetBuilder();
                if(dtd.exists()){
                    InputStream is = new FileInputStream(dtd);
                    try {
                        xb.setMetaDataSetFromDtd(is);
                    } finally {
                        is.close();
                    }
                }else{
                    xb.setColumnSensing(true);
                }
                FlatXmlDataSet pds = xb.build(data); 
                com.triniforce.db.export.MappedDataSet ds = new MappedDataSet(dbNames, pds);
                DatabaseOperation.DELETE_ALL.execute(dstConnection, ds);
                con.commit();
                DatabaseOperation.INSERT.execute(dstConnection, ds);
                con.commit();
            } finally {
                pc.returnConnection(con);
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }        
        
    }

}
