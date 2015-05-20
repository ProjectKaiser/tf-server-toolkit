/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.ddl.Delta.DeltaSchema;
import com.triniforce.db.ddl.Delta.DeltaSchemaLoader;
import com.triniforce.db.ddl.Delta.IDBNames;
import com.triniforce.db.ddl.Delta.DBMetadata.IIndexLocNames;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IProfiler;
import com.triniforce.utils.IProfilerStack;
import com.triniforce.utils.Profiler;
import com.triniforce.utils.IProfilerStack.PSI;
import com.triniforce.utils.Profiler.INanoTimer;
import com.triniforce.utils.Profiler.ProfilerStack;

public class DeltaSchemaLoaderSpeedTest extends TFTestCase {

	private Connection m_con;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		m_con = getConnection();
		
		// Помещаю профайлер в стек интерфейсов 
		Api api = new Api();
		Profiler pr = new Profiler();
		api.setIntfImplementor(IProfiler.class, pr);
		api.setIntfImplementor(IProfilerStack.class, new Profiler.ProfilerStack(pr, new INanoTimer(){
			public long get() {
				return System.nanoTime();
			}
		}));
		ApiStack.pushApi(api);
	}
	
	@Override
	protected void tearDown() throws Exception {
		ApiStack.popApi();
		super.tearDown();
		m_con.close();
	}
	
	public void test2(){
		Profiler pr = new Profiler();
		ProfilerStack prSt = new Profiler.ProfilerStack(pr, new INanoTimer(){
			public long get() {
				return System.nanoTime();
			}
		});
		PSI psi = prSt.getStackItem("groupName", "itemName");
		try{
			fun();
		}finally{
			psi.close();
		}
		trace(pr.toString());
	}
	
	void fun(){
		
	}
	
	@Override
	public void test() throws Exception {
		DeltaSchemaLoader loader = new Delta.DeltaSchemaLoader(getTabs(), new IIndexLocNames(){
			public String getShortName(String dbTabName, String dbFullName) {
				return dbFullName;
			}
		});
		DeltaSchema res;
		PSI psi = ApiAlgs.getProfItem("test", "loadSchema");
		try{
			// тестируемый метод
			res = loader.loadSchema(m_con, new IDBNames(){
				public String getAppName(String dbName) {
					return "T"+dbName;
				}
	
				public String getDbName(String appName) {
					return appName.substring(1);
				}
			});
		} finally{
			ApiAlgs.closeProfItem(psi);
		}
		
		assertNotNull(res.getTables().get("TCOUNT_FREQUENCIES"));
		// Вывод результатов профайлера на консоль
		trace(ApiStack.getInterface(IProfiler.class).toString());
	}

	private List<String> getTabs() throws SQLException, ClassNotFoundException {
		Class.forName(getTestProperty("oltpBaseClass"));
        String url =  getTestProperty("oltpBaseURL");
        String dbUserName = getTestProperty("oltpBaseUserName");
        Connection con;
        if(null != dbUserName){
	        String dbPassword = getTestProperty("oltpBasePassword");
	        con = DriverManager.getConnection(url, dbUserName, dbPassword);
        }
        else
        	con = DriverManager.getConnection(url);
        con.setAutoCommit(false);
		
		ResultSet rs = con.createStatement().executeQuery("select table_name from v_st");
		ArrayList<String> res = new ArrayList<String>();
		while(rs.next()){
			res.add("T"+rs.getString(1));
		}
		con.close();
		return res;
	}

	private Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(getTestProperty("class"));
        String url =  getTestProperty("url");
        String dbUserName = getTestProperty("username");
        Connection con;
        if(null != dbUserName){
	        String dbPassword = getTestProperty("password");
	        con = DriverManager.getConnection(url, dbUserName, dbPassword);
        }
        else
        	con = DriverManager.getConnection(url);
        con.setAutoCommit(false);
        
//		Class.forName(getTestProperty("oltpBaseClass"));
//        String url =  getTestProperty("oltpBaseURL");
//        String dbUserName = getTestProperty("oltpBaseUserName");
//        Connection con;
//        if(null != dbUserName){
//	        String dbPassword = getTestProperty("oltpBasePassword");
//	        con = DriverManager.getConnection(url, dbUserName, dbPassword);
//        }
//        else
//        	con = DriverManager.getConnection(url);
//        con.setAutoCommit(false);

        return con;
	}
}
