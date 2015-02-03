/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;

import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.extensions.IPKExtensionBase;
import com.triniforce.extensions.IPKExtensionPoint;
import com.triniforce.server.plugins.kernel.BasicServer;
import com.triniforce.server.plugins.kernel.IdDef;
import com.triniforce.server.srvapi.DataPreparationProcedure;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;
import com.triniforce.utils.TFUtils;

public class BasicServerTestCase extends TFTestCase {
    
public static class DPPProcPlugin extends DataPreparationProcedure implements IPlugin{
        
        public DPPProcPlugin() {
            super("test");
        }
    
        public void doRegistration(ISORegistration reg) throws EDBObjectException {
            reg.registerDataPreparationProcedure(this);
        }
    
        public String[] getDependencies() {
            return null;
        }
    
        public String getPluginName() {
            return null;
        }
    
        public String getProviderName() {
            return null;
        }

        public void popApi(Mode mode, ApiStack stk) {
           
        }

        public void prepareApi() {
            
        }

        public void pushApi(Mode mode, ApiStack apiStack) {
            
        }
        
        public void finitApi(){
        }

        public void finit() {
        }

        public void init() {
        }

        public void doRegistration() {
        }

        public void doExtensionPointsRegistration() {
        }
    }
	
    protected BasicServerApiEmu m_bemu = new BasicServerApiEmu(); 

    public static class Pool implements IPooledConnection {
        
        Map<Connection, StackTraceElement[]> m_conStack = new HashMap<Connection, StackTraceElement[]>();

        public BasicDataSource m_ds = null;

        public Pool(BasicDataSource ds ){
            m_ds = ds;
            m_ds.setMaxActive(50);
        }

        public Connection getPooledConnection() throws SQLException {
            Connection con = m_ds.getConnection();
            con.setAutoCommit(false);
            m_conStack.put(con, Thread.currentThread().getStackTrace());
            return con;
        }

        public void returnConnection(Connection con) throws SQLException {
            m_conStack.remove(con);
            con.close();
        }

		public int getMaxIdle() {
			return m_ds.getMaxIdle();
		}

		public int getNumIdle() {
			return m_ds.getNumIdle();
		}

		public void setMaxIdle(int maxIdle) {
			m_ds.setMaxIdle(maxIdle);
		}
    }
    
    protected int m_liUsers = 10000;
    
    protected static Pool m_pool = null;

    protected Api m_coreApi;

    protected ArrayList<IPlugin> m_plugins;

    protected BasicServer m_server;

    //protected SrvApiEmu m_emu = new SrvApiEmu();;

    public BasicServerTestCase() {
        m_plugins = new ArrayList<IPlugin>();
    }

    
    protected boolean m_wasDbModificationNeeded = false; 

    public void setCurrentTimeMillis(long time){
        m_bemu.setCurrentTimeMillis(time);
    }
    
    protected void setUp() throws Exception {
        super.setUp();  
        m_bemu.setTimeSeq(BasicServerApiEmu.START_TIME, BasicServerApiEmu.TIME_OFFSETS);
        try{
            
            if(getPool().m_ds.isClosed())
                m_pool = null;
                
	        m_startNumActive = getPool().m_ds.getNumActive();        
	
	        m_coreApi = new Api();
	
	        setCoreApiInteraces(m_coreApi);
	        
	        m_server = createServer(m_coreApi, getPlugins());
	        m_server.doRegistration();
	        if(m_server.isDbModificationNeeded()){
	            m_wasDbModificationNeeded = true;
	        	m_server.doDbModification();
	        }
	        m_server.init();
        } catch (Exception e) {
        	trace(e);
			super.tearDown();
			throw e;
		}
    }

    protected BasicServer createServer(Api api, List<IPlugin> plugins) throws Exception {
    	return new BasicServer(api, plugins);
	}

	protected void setCoreApiInteraces(Api api) {
        m_coreApi.setIntfImplementor(IPooledConnection.class, getPool());
		api.setIntfImplementor(IIdDef.class, new IdDef(ColumnType.LONG));
		api.setIntfImplementor(ITime.class, m_bemu);
    }

    boolean tabExists(Connection conn, String tabName)
            throws SQLException {
        if (UpgradeRunner.getDbType(conn).equals(UpgradeRunner.DbType.DERBY))
            tabName = tabName.toUpperCase();

        ResultSet rs = conn.getMetaData().getTables(null, null, tabName, null);
        boolean res;
        try {
            res = rs.next();
        } finally {
            rs.close();
        }
        return res;
    }

    protected List<IPlugin> getPlugins() {
        return m_plugins;
    }

    protected void addPlugin(IPlugin plugin) {
        m_plugins.add(plugin);
    }

    protected void checkResourcesConnections(){
        if (m_startNumActive != m_pool.m_ds.getNumActive()) {
            int m_newNumActive = m_pool.m_ds.getNumActive();
            String s = "";
            for (StackTraceElement[] trace : getPool().m_conStack.values()) {
                for (StackTraceElement tr : trace) {
                    s = s + tr.toString() + "\n";
                }
                ApiAlgs.getLog(this).error(s);
            }
            m_pool = null;
            fail("Number of active connections changed from "
                    + m_startNumActive + " to " + m_newNumActive+", caused by:\n" + s);
        }
        
    }
    
    @Override
    protected void checkResources(){
        super.checkResources();
        checkResourcesConnections();        
    }
    
    int m_startNumActive = 0;
    
    protected void tearDown() throws Exception {
        if( null != m_server){
            m_server.finit();
        }
        m_server = null;
        m_coreApi = null;
        m_plugins = null;
        m_bemu = null;
        super.tearDown();
    }

    public Pool getPool(){
        if (m_pool == null)
            m_pool = new Pool(getDataSource());
        return m_pool;
    }
    
    public BasicServer getServer(){
    	return m_server;
    }

    public String login(String name, String pwd) {
        return null;
    }

    public void logout(String name, String sID) throws Exception {
    }

    public static void checkExtensionClass(IPKExtensionPoint ep, List<String> problems) {
		Class ec = ep.getExtensionClass();
		if (null == ec){
			problems.add("Empty extension class for " + ep.getId());
		}
	}
    
    public static void checkWiki(IPKExtensionBase eb, String epId, List<String> problems) {
		String wiki = eb.getWikiDescription();
		if (null == wiki || "".equals(wiki)){
			problems.add("Empty wiki description for " + (null != epId ? (epId + "/") : "")
					+ eb.getId());
		}
	}
    
    protected static Set<Class> m_allowedEmptyWiki = new HashSet<Class>();
    
    public static void checkExtensions(TFTestCase tc, IBasicServer srv){
    	List<String> problems = new ArrayList<String>();
    	
   	
    	for(IPKExtensionPoint ep: srv.getExtensionPoints().values()){
    		checkWiki(ep, null, problems);
    		checkExtensionClass(ep, problems);
//    		for(IPKExtension e: ep.getExtensions().values()){
//    			tc.trace(e.getId());
//    		    if(!m_allowedEmptyWiki.contains(e.getObjectClass())){
//    		        checkWiki(e, ep.getId(), problems);
//    		    }
//    		}
    	}
    	if(problems.size() > 0){
    		StringBuffer strProblems = new StringBuffer();
    		for(String strProblem: problems){
    			strProblems.append(strProblem + TFUtils.lineSeparator());
    		}
    		fail("Problems with extensions: \n" + strProblems);
    	}
    }
}
