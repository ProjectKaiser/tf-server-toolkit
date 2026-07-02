/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.apache.commons.dbcp2.BasicDataSource;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.SrvSmartTranFactory.EPoolConnectionError;
import com.triniforce.server.plugins.kernel.TFPooledConnection.IStackTraceInfo;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.IPooledConnection.StackTraceRec;
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

            // pop() must remove only the transaction API frame and leave the caller API on top.
            assertSame(topApi, ApiStack.getThreadApiContainer().getStack().peek());
        } finally{
            ApiStack.popApi();
        }
    }

    // Design: check out one real pooled connection, make transaction close throw,
    // then verify pop() propagates that failure while returning the connection.
    public void testPopReturnsConnectionWhenTranCloseThrows() throws SQLException {
        Mockery context = new Mockery();
        final ISrvSmartTran trn = context.mock(ISrvSmartTran.class);
        final RuntimeException closeError = new RuntimeException("close failed");
        Connection c = getDataSource().getConnection();
        try {
            BasicDataSource ds = new BasicDataSource() {
                public Connection getConnection() throws SQLException {
                    return c;
                };
            };
            TFPooledConnection pool = new TFPooledConnection(ds, 10);

            // Start from the real failure shape: one connection is checked out.
            Connection con = pool.getPooledConnection();
            assertEquals(1, pool.getTakenConnectionPoints().size());

            // The parent API owns the pool; the transaction API owns the checked-out connection.
            Api topApi = new Api();
            topApi.setIntfImplementor(IPooledConnection.class, pool);
            ApiStack.pushApi(topApi);
            Api lowApi = new Api();
            lowApi.setIntfImplementor(Connection.class, con);
            lowApi.setIntfImplementor(ISrvSmartTran.class, trn);
            ApiStack.pushApi(lowApi);

            try{
                context.checking(new Expectations(){{
                    one(trn).close(); will(throwException(closeError));
                }});

                try {
                    m_factory.pop();
                    fail();
                } catch (RuntimeException e) {
                    // The cleanup must not hide the original transaction close failure.
                    assertSame(closeError, e);
                }
                context.assertIsSatisfied();

                // Most important assertion: throwing close must not leave the pool holder behind.
                assertEquals(0, pool.getTakenConnectionPoints().size());
            } finally{
                ApiStack.popApi();
            }
        } finally{
            if(!c.isClosed()) {
                c.close();
            }
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

    boolean DOTHROW = false;
    public void testPushError() throws SQLException {
        Connection c = getDataSource().getConnection();
        try {

            BasicDataSource ds = new BasicDataSource() {
                public Connection getConnection() throws SQLException {
                    if(DOTHROW)
                        throw new SQLException("testthrow");
                    return c;
                };
            };
            TFPooledConnection pool = new TFPooledConnection(ds, 10);

            ApiStack.pushInterface(IStackTraceInfo.class, new IStackTraceInfo() {
                @Override
                public String getInfo() {
                    return "t000";
                }

            });
            try {

                Mockery context = new Mockery();
                final Connection con = context.mock(Connection.class);
                ISrvPrepSqlGetter getter = context.mock(ISrvPrepSqlGetter.class);
                Api api = new Api();
                api.setIntfImplementor(IPooledConnection.class, pool);
                api.setIntfImplementor(ISrvPrepSqlGetter.class, getter);
                api.setIntfImplementor(ISrvSmartTranFactory.class, m_factory);
                ApiStack.pushApi(api);

                try{

                    m_factory.push();
                    DOTHROW = true;
                    try {
                        m_factory.push();
                        fail();
                    }catch(EPoolConnectionError e) {
                        assertEquals(1, e.getPoints().size());
                        assertEquals("t000", e.getPoints().iterator().next().getInfo());
                        assertTrue(e.getMessage().contains("Total in conStack: 1"));
                    }
                    finally{
                        ApiStack.popApi();
                    }
                } finally{
                    ApiStack.popApi();
                }
            }finally{
                ApiStack.popInterface(1);
            }
        }finally {
            c.close();
            DOTHROW = false;
        }
    }

    public void testTakenConnectionPointsStableAfterReturn() throws SQLException {
        Connection c = getDataSource().getConnection();
        try {
            BasicDataSource ds = new BasicDataSource() {
                public Connection getConnection() throws SQLException {
                    return c;
                };
            };
            TFPooledConnection pool = new TFPooledConnection(ds, 10);
            Connection pooled = pool.getPooledConnection();
            Collection<StackTraceRec> points = pool.getTakenConnectionPoints();
            Iterator<StackTraceRec> it = points.iterator();
            assertTrue(it.hasNext());

            pool.returnConnection(pooled);
            try {
                assertNotNull(it.next());
            } catch (ConcurrentModificationException e) {
                fail("diagnostic points must be stable while connections are returned");
            }
        }finally {
            if(!c.isClosed()) {
                c.close();
            }
        }
    }

}
