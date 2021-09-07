package com.triniforce.server.plugins.kernel;
/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */


import java.io.File;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.dbcp2.BasicDataSource;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.db.test.BasicServerTestCase.Pool;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.BasicServer.ServerException;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class BasicServerTrStartTest extends TFTestCase {
	
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
		ds.setMaxWaitMillis(100L);		
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


	private String createDb(String cls) throws Exception {
		copyTestResources(new String[]{"EMPTY.FDB"}, getTempTestFolder());
		String fn = new File(getTempTestFolder(),"EMPTY.FDB").getAbsolutePath();
		Class.forName(getTestProperty("class"));
		
		
		return "jdbc:firebirdsql:localhost/3050:"+fn;
	}
}
