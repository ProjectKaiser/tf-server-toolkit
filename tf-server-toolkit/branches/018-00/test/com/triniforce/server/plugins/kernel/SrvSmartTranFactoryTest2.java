/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISrvPrepSqlGetter;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class SrvSmartTranFactoryTest2 extends TFTestCase {
    
    SrvSmartTranFactory m_factory = new SrvSmartTranFactory();

    public void testPop() throws SQLException {
        Mockery context = new Mockery();
        final IPooledConnection pool = context.mock(IPooledConnection.class);
        final Connection con = context.mock(Connection.class);
        final ISrvSmartTran trn = context.mock(ISrvSmartTran.class);
        Api topApi = new Api();
        topApi.setIntfImplementor(IPooledConnection.class, pool);
        ApiStack.pushApi(topApi);
        Api lowApi = new Api();
        lowApi.setIntfImplementor(Connection.class, con);
        lowApi.setIntfImplementor(ISrvSmartTran.class, trn);
        ApiStack.pushApi(lowApi);

        try{
            context.checking(new Expectations(){{
                one(pool).returnConnection(con);
                one(trn).close();
            }});
            
            m_factory.pop();
            context.assertIsSatisfied();
            
            assertSame(topApi, ApiStack.getThreadApiContainer().getStack().peek());
        } finally{
            ApiStack.popApi();
        } 
    }

    public void testPush() throws SQLException {
        Mockery context = new Mockery();
        final IPooledConnection pool = context.mock(IPooledConnection.class);
        final Connection con = context.mock(Connection.class);
        ISrvPrepSqlGetter getter = context.mock(ISrvPrepSqlGetter.class); 
        Api api = new Api();
        api.setIntfImplementor(IPooledConnection.class, pool);
        api.setIntfImplementor(ISrvPrepSqlGetter.class, getter);
        api.setIntfImplementor(ISrvSmartTranFactory.class, m_factory);
        ApiStack.pushApi(api);

        try{
            context.checking(new Expectations(){{
                one(pool).getPooledConnection();
                will(returnValue(con));
                one(con).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }});
            
            m_factory.push();
            context.assertIsSatisfied();
            
            Connection con2 = ApiStack.getApi().getIntfImplementor(Connection.class);
            assertSame(con, con2);
            assertNotNull(ApiStack.getApi().queryIntfImplementor(ISrvSmartTran.class));
            
            ApiStack.popApi();
            assertSame(api, ApiStack.getThreadApiContainer().getStack().peek());
        } finally{
            ApiStack.popApi();
        }
    }

}
