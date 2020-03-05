/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.dbcp.BasicDataSource;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.management.FBManager;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.db.test.BasicServerTestCase.Pool;
import com.triniforce.server.plugins.kernel.BasicServer.ERegistratorRunning;
import com.triniforce.server.plugins.kernel.BasicServer.ServerException;
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


//	@Override
	public void test() throws Exception {
		BasicDataSource ds = getDataSource();
		startServerWithRegistrator(ds);
	}
	
	public void testStartServerInDifferentLocales() throws Exception{
		Locale[] locales2test = new Locale[]{
				new Locale("tr", "TR")
		};
		
		Locale loc = Locale.getDefault();
		try{
			
			for (Locale locale : locales2test) {
				Locale.setDefault(locale);
				BasicDataSource pool = new BasicDataSource();
				String cls = "org.firebirdsql.jdbc.FBDriver";
		        pool.setDriverClassName(cls);
		        pool.setUrl(createDb(cls));
		        String dbUserName = "UNTILLUSER";
		        String dbPassword = "1945";
		        pool.setUsername(dbUserName);
		        pool.setPassword(dbPassword);

				IBasicServer srv = startServer(pool);				
				srv.finit();
			}
		}finally{
			Locale.setDefault(loc);
		}
	}


	private IBasicServer startServer(BasicDataSource ds) throws ServerException, EDBObjectException, SQLException {
		ds.setMaxWait(100L);		
		Pool pool = new BasicServerTestCase.Pool(ds);
		Api baseApi = new Api();
		final BasicServer srv = new BasicServer();
		srv.doPluginsRegistration();
		baseApi.setIntfImplementor(IPooledConnection.class, pool);
		
		Runnable runner = new Runnable(){
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
		};
		
		srv.setBaseApi(baseApi);
		srv.doRegistration();

		ApiStack.pushInterface(IBasicServer.class, srv);
		try{
			runner.run();
		}finally{
			ApiStack.popInterface(1);
		}
		return srv;
	}


	private IBasicServer startServerWithRegistrator(BasicDataSource ds) {
		ds.setMaxWait(100L);		
		Pool pool = new BasicServerTestCase.Pool(ds);
		Api baseApi = new Api();
		final BasicServer srv = new BasicServer();
		srv.doPluginsRegistration();
		baseApi.setIntfImplementor(IPooledConnection.class, pool);
		
		Runnable runner = new Runnable(){
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
		};

		srv.startRegistrator(baseApi, runner);
		trace("registration thread started");
		srv.waitRegistrator(10000L);

		return srv;
	}


	private String createDb(String cls) throws Exception {
		copyTestResources(new String[]{"EMPTY.FDB"}, getTempTestFolder());
		String fn = new File(getTempTestFolder(),"EMPTY.FDB").getAbsolutePath();
		Class.forName(getTestProperty("class"));
		
		
		return "jdbc:firebirdsql:localhost/3050:"+fn;
	}
}
