/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.db.test.BasicServerTestCase.Pool;
import com.triniforce.server.plugins.kernel.BasicServer.ERegistratorRunning;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class BasicServerStartTest extends TFTestCase {
	
	boolean bCall = false;

	public void testStartRegistrator() throws InterruptedException, SQLException{
		Api baseApi = new Api();
		Pool pool = new BasicServerTestCase.Pool(getDataSource());
		{
			final BasicServer srv = new BasicServer();
			srv.doPluginsRegistration();
			baseApi.setIntfImplementor(IPooledConnection.class, pool);
			baseApi.setIntfImplementor(String.class, "test_str-0001251");
			srv.startRegistrator(baseApi, new Runnable(){
				public void run() {
					assertSame(srv, ApiStack.getInterface(IBasicServer.class));
					bCall = true;
				}
			});
			assertFalse(srv.isRegistered());
			assertFalse(bCall);
			Thread.sleep(1000L);
			assertTrue(srv.isRegistered());
			assertTrue(bCall);
			
			try{
				srv.startRegistrator(baseApi, null);
				fail();
			}catch(ERegistratorRunning e){
			}
			
		}
		BasicServer srv;
		{
			srv = new BasicServer();
			Mockery ctx = new Mockery();
			final IPooledConnection pool2 = ctx.mock(IPooledConnection.class);
			final Connection con = ctx.mock(Connection.class);
			ctx.checking(new Expectations(){{
				allowing(pool2).getPooledConnection(); will(returnValue(con));
				allowing(pool2).returnConnection(con);
				allowing(con).getMetaData(); will(throwException(new SQLException()));
			}});
			baseApi.setIntfImplementor(IPooledConnection.class, pool2);
			srv.startRegistrator(baseApi, new Runnable(){
				public void run() {
				}
			});
			Thread.sleep(1000L);
			assertFalse(srv.isRegistered());
			baseApi.setIntfImplementor(IPooledConnection.class, pool);
			Thread.sleep(5000L);
			assertTrue(srv.isRegistered());
		}
		
		{
			srv = new BasicServer();
			Mockery ctx = new Mockery();
			final IPooledConnection pool2 = ctx.mock(IPooledConnection.class);
			final Connection con = ctx.mock(Connection.class);
			ctx.checking(new Expectations(){{
				allowing(pool2).getPooledConnection(); will(returnValue(con));
				allowing(pool2).returnConnection(con);
				allowing(con).getMetaData(); will(throwException(new SQLException()));
			}});
			baseApi.setIntfImplementor(IPooledConnection.class, pool2);
			srv.startRegistrator(baseApi, new Runnable(){
				public void run() {
				}
			});
			assertFalse(srv.isRegistered());
			
			srv.stopAndWaitRegistrator();
			
			baseApi = new Api();
			baseApi.setIntfImplementor(IPooledConnection.class, pool);
			srv.startRegistrator(baseApi, new Runnable(){
				public void run() {
				}
			});
			Thread.sleep(1000);
			assertTrue(srv.isRegistered());
		}

		srv.stopAndWaitRegistrator();
		srv.stopAndWaitRegistrator();

	}


	@Override
	public void test() throws Exception {
		BasicDataSource ds = getDataSource();
		ds.setMaxWait(100L);
		Pool pool = new BasicServerTestCase.Pool(ds);
		Api baseApi = new Api();
		final BasicServer srv = new BasicServer();
		srv.doPluginsRegistration();
		baseApi.setIntfImplementor(IPooledConnection.class, pool);

		srv.startRegistrator(baseApi, new Runnable(){
			public void run() {
				IBasicServer isrv = ApiStack.getInterface(IBasicServer.class);
				if(isrv.isDbModificationNeeded()){
					try {
						isrv.doDbModification();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				isrv.initAndStart();
				trace("server started successfully");
			}
		});
		trace("registration thread started");
		srv.waitRegistrator(0L);
	}
}
