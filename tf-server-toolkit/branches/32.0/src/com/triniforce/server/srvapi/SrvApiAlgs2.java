/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.srvapi;

import java.sql.Connection;
import java.sql.SQLException;

import com.triniforce.db.dml.StmtContainer;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IApi;

public class SrvApiAlgs2 {

    /**
     * 
     * @return Commited connection without autoCommit mode from
     *         IPooledConnection installed in current SrvApi
     */
    public static Connection getPooledConnection() {
        return getPooledConnection(ApiStack.getApi());
    }

    /**
     * @return installed IServerTran interface
     */
    public static ISrvSmartTran getIServerTran() {
        return (ISrvSmartTran) ApiStack.getApi().getIntfImplementor(
                ISrvSmartTran.class);
    }

    /**
     * @return installed ISODbInfo interface
     */
    public static ISODbInfo getISODbInfo() {
        return (ISODbInfo) ApiStack.getApi().getIntfImplementor(ISODbInfo.class);
    }

    /**
     * @return installed ISOQuery interface
     */
    public static ISOQuery getISOQuery() {
        return (ISOQuery) ApiStack.getApi().getIntfImplementor(ISOQuery.class);
    }

    /**
     * @return installed IPrepSqlGetter interface
     */
    public static ISrvPrepSqlGetter getIPrepSqlGetter() {
        return (ISrvPrepSqlGetter) ApiStack.getApi().getIntfImplementor(
                ISrvPrepSqlGetter.class);
    }

    /**
     * @return installed IPrepSqlGetter interface
     */
    public static ISrvPrepSqlGetter queryIPrepSqlGetter() {
        Object o = ApiStack.getApi().queryIntfImplementor(ISrvPrepSqlGetter.class);
        if (null == o)
            return null;
        return (ISrvPrepSqlGetter) o;
    }

    /**
     * Get commited connection without autoCommit mode from IPooledConnection
     * installed in api
     * 
     * @param api
     *            SrvApi where installed IPooledConnection
     * @return Connection
     * @throws SQLException
     */
    public static Connection getPooledConnection(IApi api) {
        try {
            IPooledConnection pool = api
                    .getIntfImplementor(IPooledConnection.class);
            Connection conn = pool.getPooledConnection();
            boolean bCommited = false;
            try {
                conn.setAutoCommit(false);
                conn.commit();
                bCommited = true;
            } finally {
                if (!bCommited)
                    pool.returnConnection(conn);
            }
            return conn;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

    public static void returnPooledConnection(IApi api, Connection conn) {
        IPooledConnection pool = api
                .getIntfImplementor(IPooledConnection.class);
        try {
            pool.returnConnection(conn);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

    public static StmtContainer getStmtContainer() {
        Connection con = ApiStack.getApi().getIntfImplementor(Connection.class);
        return new StmtContainer(getIServerTran(), con, getIPrepSqlGetter());
    }
    
    public static long generateId(){
        IIdGenerator idGen = ApiStack.getApi().getIntfImplementor(IIdGenerator.class);
        return idGen.getKey();
    }

    public static ISrvSmartTranFactory getISrvTranFactory() {
        return ApiStack.getInterface(ISrvSmartTranFactory.class);
        
    }
}
