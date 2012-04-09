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
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;

import com.triniforce.db.ddl.UpgradeRunner;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.server.plugins.kernel.BasicServer;
import com.triniforce.server.srvapi.DataPreparationProcedure;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class BasicServerTestCase extends TFTestCase {

    public static abstract class DPPProcPlugin extends DataPreparationProcedure implements IPlugin{
    	
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
	}
	
    public static class Pool implements IPooledConnection {

        public BasicDataSource m_ds = null;

        public Pool(BasicDataSource ds ){
            m_ds = ds;
        }

        public Connection getPooledConnection() throws SQLException {
            Connection con = m_ds.getConnection();
            con.setAutoCommit(false);
            return con;
        }

        public void returnConnection(Connection con) throws SQLException {
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

    int m_apiStackCnt;

    protected void setUp() throws Exception {
        m_apiStackCnt = ApiStack.getThreadApiContainer().getStack().size();
        super.setUp();

        m_coreApi = new Api();
//        m_coreApi.setIntfImplementor(ITime.class, m_emu);
        m_coreApi.setIntfImplementor(IPooledConnection.class, getPool());
//        m_coreApi.setIntfImplementor(IServerParameters.class, m_emu);

        setCoreApiInteraces(m_coreApi);

        m_server = createServer(m_coreApi, getPlugins());
        m_server.doRegistration();
        if(m_server.isDbModificationNeeded())
        	m_server.doDbModification();
    }

    protected BasicServer createServer(Api api, List<IPlugin> plugins) throws Exception {
    	return new BasicServer(api, plugins);
	}

	protected void setCoreApiInteraces(Api api) {
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

    protected void tearDown() throws Exception {
        super.tearDown();
        assertEquals(m_apiStackCnt, ApiStack.getThreadApiContainer().getStack()
                .size());
        m_server = null;
        m_coreApi = null;
        m_plugins = null;
    }

    public Pool getPool() throws Exception {
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

}
