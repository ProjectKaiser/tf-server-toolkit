/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.test.DBTestCase;
import com.triniforce.server.srvapi.DataPreparationProcedure;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IEntity;
import com.triniforce.utils.ApiStack;

public class ServerTest extends DBTestCase {

    protected static class TestPooledConnection implements IPooledConnection{
        Connection m_conn = null;
        
        boolean m_bCalled = false;
        boolean m_bReleased = false;
        
        public TestPooledConnection(Connection conn) {
            m_conn = conn;
        }
        
        public Connection getPooledConnection() {
            try {
                m_bCalled = true;
                return m_conn;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        public void returnConnection(Connection con) {
            if(m_bCalled)
                m_bReleased = true;
        }

		public int getMaxIdle() {
//			fail();
			return 0;
		}

		public int getNumIdle() {
			fail();
			return 0;
		}

		public void setMaxIdle(int maxIdle) {
//			fail();
		}

		@Override
		public void close() {
			try {
				m_conn.close();
			} catch (SQLException e) {
				ApiAlgs.rethrowException(e);
			}
		}

		@Override
		public String getInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<StackTraceRec> getTakenConnectionPoints() {
			// TODO Auto-generated method stub
			return null;
		}        
    }

    protected static class TestPlugin implements IPlugin{

        protected boolean bRegistered = false;
        
        protected IEntity[] m_defs = null;        

        public TestPlugin(IEntity[] defs) {
            m_defs = defs;
        }

        public TestPlugin() {
        }

        public void doRegistration(ISORegistration reg) throws EDBObjectException{
            this.bRegistered  = true;
            //assertNotNull(ApiStack.getApi().queryIntfImplementor(ISrvSmartTranFactory.class));
            assertNotNull(ApiStack.getApi().queryIntfImplementor(ISORegistration.class));
         
            if(m_defs!=null){
                for(IEntity def : m_defs){
                    if(def instanceof TableDef)
                        reg.registerTableDef((TableDef) def);
                    else if(def instanceof DataPreparationProcedure)
                        reg.registerDataPreparationProcedure((DataPreparationProcedure) def);
                    else if(def instanceof UpgradeProcedure)
                        reg.registerUpgradeProcedure((UpgradeProcedure) def);
                }
            }
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

		public void pushApi(Mode mode, ApiStack stk) {
		}

		public void finitApi() {
			
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
    
    protected Api m_coreApi;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        //SrvApiEmu emu = new SrvApiEmu();

        m_coreApi = new Api();
        m_coreApi.setIntfImplementor(IPooledConnection.class, new TestPooledConnection(getConnection()));
        //m_coreApi.setIntfImplementor(IServerParameters.class, emu);
        
    }

    
}
