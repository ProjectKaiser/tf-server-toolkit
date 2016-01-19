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
import java.util.Collections;
import java.util.Comparator;
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
	
    private BasicServerApiEmu m_bemu = new BasicServerApiEmu(); 

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

		public void setDataSource(BasicDataSource dataSource) {
			m_ds = dataSource;
		}
    }
    
    protected int m_liUsers = 10000;
    
    protected static Pool m_pool = null;

    protected Api m_coreApi;

    protected ArrayList<IPlugin> m_plugins;

    protected static BasicServer m_server;

    private static List<IPlugin> SERVER_INSTALLED_PLUGINS;
    private static Comparator<IPlugin> PLUGIN_COMPARATOR = new Comparator<IPlugin>(){
		@Override
		public int compare(IPlugin o1, IPlugin o2) {
			int res = o1.getClass().getName().compareTo(o2.getClass().getName());
			if(0 == res){
				String one = o1.getPluginName();
				String two = o2.getPluginName();
				if (one == null ^ two == null) {
			        res = (one == null) ? -1 : 1;
			    }

				else if (one == null && two == null) {
			        res = 0;
			    }
				else
					res = one.compareTo(two); 
			}
			return res;
		}
    };;

    //protected SrvApiEmu m_emu = new SrvApiEmu();;

    protected boolean m_bFinitServer = false;
    protected boolean m_bRestartOnSetup = false;

    public BasicServerTestCase() {
        m_plugins = new ArrayList<IPlugin>();
    }

    
    protected boolean m_wasDbModificationNeeded = false; 

    public void setCurrentTimeMillis(long time){
        getBasicSrvApiEmu().setCurrentTimeMillis(time);
    }
    
    protected BasicServerApiEmu getBasicSrvApiEmu() {
    	ITime res = getServer().getCoreApi().getIntfImplementor(ITime.class);
//		ITime res = ApiStack.getInterface(ITime.class);
		if(res instanceof BasicServerApiEmu){
			return (BasicServerApiEmu) res;
		}
		return null;
	}

	protected void setUp() throws Exception {
        super.setUp();  

        if(isNeededServerRestart())
        	finitServer();
        
        
        if(null == m_server){
	        try{
	        	m_bemu = new BasicServerApiEmu();
	            m_bemu.setTimeSeq(BasicServerApiEmu.START_TIME, BasicServerApiEmu.TIME_OFFSETS);
	                
		        m_startNumActive = getPool().m_ds.getNumActive();        
		
		        m_coreApi = new Api();
		        setCoreApiInteraces_internal(m_coreApi);
		        setCoreApiInteraces(m_coreApi);
		        
		        m_server = createServer(m_coreApi, getPlugins());
		        m_server.doRegistration();
		        if(m_server.isDbModificationNeeded()){
		            m_wasDbModificationNeeded = true;
		        	m_server.doDbModification();
		        }
		        m_server.init();
		        SERVER_INSTALLED_PLUGINS = new ArrayList<IPlugin>(m_server.getPlugins());
		        Collections.sort(SERVER_INSTALLED_PLUGINS, PLUGIN_COMPARATOR);
	        } catch (Exception e) {
	        	trace(e);
				super.tearDown();
				throw e;
			}
        }
        
    }

    private void finitServer() {
        if( null != m_server){
        	try{
        		boolean bCount = isCountErrorLogs();
        		countErrorLogs(false);
        		try{
        			m_server.stopAndFinit();
        		}finally{
	        		if(bCount)
	        			countErrorLogs(true);
        		}
        	}catch(Exception e){
        		ApiAlgs.getLog(this).trace("server finit error", e);
        	}
        }
        m_server = null;
        ApiAlgs.getLog(this).trace("server finited");
	}
    
    protected boolean isNeededServerRestart() {
		if(m_bRestartOnSetup)
			return true;
		boolean res = false; 
		if(null != m_server){
			List<IPlugin> plugins = getPlugins();
			
			for(IPlugin plugin : getPlugins()){
				res = Collections.binarySearch(SERVER_INSTALLED_PLUGINS, plugin, PLUGIN_COMPARATOR) < 0;
				if (res) break;
			}
			
			if(res){
				ApiAlgs.getLog(this).info("NEED_RESTART: " + plugins.toString());
			}
			
			if(!res){
				Api api = new Api();
				setCoreApiInteraces(api);
				res = !api.getImplementors().isEmpty();
			}
			
			
		}

        if(getPool().m_ds.isClosed()){
        	getPool().setDataSource(getDataSource());
        	res = true;
        }

		
		return res;
	}

	protected BasicServer createServer(Api api, List<IPlugin> plugins) throws Exception {
    	return new BasicServer(api, plugins);
	}

	protected void setCoreApiInteraces(Api api) {
    }
	
	protected void setCoreApiInteraces_internal(Api api) {
		
		if (getPool().m_ds.isClosed()){
			m_pool = null;
		}
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
    	if(m_bFinitServer){
    		finitServer();
    	}
//        m_coreApi = null;
//        m_plugins = null;
//        m_bemu = null;
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
    
    protected void restartServerOnSetup(){
    	m_bRestartOnSetup = true;
    }
    
    protected void finitServerOnTearDown(){
    	m_bFinitServer = true;
    }
}
