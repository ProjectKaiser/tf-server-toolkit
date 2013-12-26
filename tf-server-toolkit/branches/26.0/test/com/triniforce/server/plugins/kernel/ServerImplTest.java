/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import java.sql.Connection;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.ddl.ActualStateBL;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.server.plugins.kernel.BasicServer.EInvalidServerState;
import com.triniforce.server.plugins.kernel.BasicServer.ServerException;
import com.triniforce.server.srvapi.DataPreparationProcedure;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.server.srvapi.IPooledConnection;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IServerMode;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.UpgradeProcedure;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IEntity;
import com.triniforce.utils.ITime;

/**
 * 
 */
public class ServerImplTest extends ServerTest {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#Server(com.triniforce.utils.ApiStack, java.util.List)}.
     * @throws Throwable 
     */
    public void testServer() throws Throwable {
        
        IPlugin testPlugin = new IPlugin(){
            public void doRegistration(ISORegistration reg){}
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
			public void finit() {
				
			}
			public void init() {

			}
            public void doRegistration() {
            }
            public void doExtensionPointsRegistration() {
            }
        };      
        {
            BasicServer server = new BasicServer(m_coreApi, Arrays.asList(testPlugin));
            
            {   // test initial state

                { // ITime
                    long st = System.currentTimeMillis();
                    long cur = server.currentTimeMillis();
                    long end = System.currentTimeMillis();            
                    assertTrue(st<= cur && cur <= end);
                }
                { // ISOQuery
                    assertEquals(0, server.getEntities(TableDef.class).size());
                    
                    try{
                        assertNull(server.getEntity("some.entity.name"));
                        fail();
                    } catch(ISOQuery.EServerObjectNotFound e){}
                    
                    assertNull(server.quieryEntity("some.other.entity"));
                }
                {
                    assertFalse(server.isRegistered());
                    
                    try{
                        assertFalse(server.isDbModificationNeeded());
                        fail();
                    } catch(ServerException e){}
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    public void  testServerRegistration() throws Exception{
    	Mockery ctx = new Mockery();
    	final IPlugin pl = ctx.mock(IPlugin.class);
    	final BasicServer srv = new BasicServer(m_coreApi, Arrays.asList(pl));
    	ctx.checking(new Expectations(){{
    		one(pl).doRegistration(srv);
    		one(pl).prepareApi();
    		one(pl).pushApi(with(Mode.Registration), with(any(ApiStack.class)));
    		one(pl).popApi(Mode.Registration, null);
    	}});
    	srv.doRegistration();
    	
    	ctx.assertIsSatisfied();
    	
    	ctx.checking(new Expectations(){{
    		one(pl).pushApi(with(Mode.Registration), with(any(ApiStack.class)));
    	}});
    	
    	srv.enterMode(Mode.Registration);
    	try{
    		ctx.assertIsSatisfied();
    	}finally{
	    	ctx.checking(new Expectations(){{
	    		one(pl).popApi(Mode.Registration, null);
	    	}});
	    	try{
	    		srv.leaveMode();
	    		ctx.assertIsSatisfied();
	    	}finally{
	    	}
    	}
    }
    
    public void testBasicServerCorePlugin() throws Exception{
    	BasicServer srv = new BasicServer(m_coreApi, null);
    	srv.enterMode(Mode.Registration);
    	try{
    		ApiStack.getInterface(ISrvSmartTranFactory.class);
    		IServerMode mode = ApiStack.getInterface(IServerMode.class);
    		assertEquals(Mode.Registration, mode.getMode());
    	}finally{
    		srv.leaveMode();
    	}
    	assertNull(ApiStack.queryInterface(ISrvSmartTranFactory.class));
    	
    	srv.doRegistration();
    	if(srv.isDbModificationNeeded())
    		srv.doDbModification();
    	
    	srv.enterMode(Mode.Running);
    	try{
    		ApiStack.getInterface(ISrvSmartTran.class);
    		ApiStack.getInterface(Connection.class);
    	}finally{
    		srv.leaveMode();
    	}
        
        srv.enterMode(Mode.Running);
        try{
            ISrvSmartTranFactory.Helper.pop();
        }finally{
            srv.leaveMode();
        }        

    }
    
    static class TestDPP1 extends DataPreparationProcedure{
    	public TestDPP1() {
			super(TestDPP1.class.getName());
		}
    	static boolean bCall = false;
    	
    	@Override
    	public void run() throws Exception {
    		bCall= true;
    	}
    }

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#doRegistration()}.
     * @throws Throwable 
     */
    public void testDoRegistration() throws Throwable {
    	getConnection().commit();
    	ApiAlgs.getLog(this).trace("numActive: "+getDataSource().getNumActive());
        //CleanDatabase.run(getConnection(), ApiAlgs.getLog(this));
        TestPlugin plugin = new TestPlugin();
        BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{plugin}));
        {
            server.doRegistration();
            
            TestPooledConnection conn = m_coreApi.getIntfImplementor(IPooledConnection.class);
            // request connection and release after
            assertTrue(conn.m_bCalled);
            assertTrue(conn.m_bReleased);
            // must be registered plugin
            assertTrue(plugin.bRegistered);
            assertNull(m_coreApi.queryIntfImplementor(Connection.class));
            assertTrue(server.isRegistered());
            
            //assertTrue(server.isDbModificationNeeded());
        }
        {
            try{
                server.doRegistration();
                fail();
            } catch(ServerException e){}
        }
        
        // registered procedures called only once
//        {
//        	BasicServer srv = new BasicServer(m_coreApi);
//        	plugin = new TestPlugin(new IEntity[]{new TestDPP1()});
//        	srv.addPlugin(plugin);
//        	srv.doRegistration();
//        	srv.setRunDPProcedure(true);
//        	srv.doDbModification();
//        	assertTrue(TestDPP1.bCall);
//        	
//        	TestDPP1.bCall=false;
//        	
//        	srv = new BasicServer(m_coreApi);
//        	plugin = new TestPlugin(new IEntity[]{new TestDPP1()});
//        	srv.addPlugin(plugin);
//        	srv.doRegistration();
//        	srv.doDbModification();
//        	assertFalse(TestDPP1.bCall);
//        }
    }
    
    public void testIsDbModificationNeeded() throws Throwable{
        //CleanDatabase.run(this);
        {
            BasicServer server = new BasicServer(m_coreApi, null);
            
            try{
                server.isDbModificationNeeded();
                fail("before reg");
            }catch(ServerException e){}
            
            server.doRegistration();
            //assertTrue("modify initial state", server.isDbModificationNeeded());
            
            server.doDbModification();
            assertFalse("after modification", server.isDbModificationNeeded());
        }
        {
            TableDef def = new TableDef("com.triniforce.server.plugins.kernel.ServerTest.testIsDbModificationNeeded");
            def.addScalarField(1, "f1", ColumnType.INT, true, 0);
            TestPlugin plugin = new TestPlugin(new TableDef[]{def});
            BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{plugin}));            
            
            server.doRegistration();
            server.doDbModification();
            assertFalse("after modification", server.isDbModificationNeeded());
            def.addPrimaryKey(2, "pk", new String[]{"f1"});
            assertTrue("after modification", server.isDbModificationNeeded());            
        }
    }
    
    public void testDoDbModification() throws Throwable{
        
        TableDef def1 = new TableDef("com.triniforce.server.plugins.kernel.ServerTest.testDoDbModification1");
        def1.addScalarField(1, "f1", ColumnType.INT, true, 0);        
        TableDef def2 = new TableDef("com.triniforce.server.plugins.kernel.ServerTest.testDoDbModification2");
        def2.addScalarField(1, "f1", ColumnType.INT, true, 0);
        TestPlugin plugin1 = new TestPlugin(new TableDef[]{def1, def2});
        def2.addScalarField(2, "f2", ColumnType.INT, true, 0);        
        TableDef def3 = new TableDef("com.triniforce.server.plugins.kernel.ServerTest.testDoDbModification3");
        def3.addScalarField(1, "f1", ColumnType.INT, true, 0);
        TestPlugin plugin2 = new TestPlugin(new TableDef[]{def2, def3});
        BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{plugin1, plugin2}));
        
        {   //no registration
            try{
                server.doDbModification();
                fail();
            } catch(ServerException e){}
        }
        {
            server.doRegistration();
            server.doDbModification();
            assertFalse(server.isDbModificationNeeded());
            
            assertNotNull(server.getTableDbName("com.triniforce.server.plugins.kernel.ServerTest.testDoDbModification1"));
            assertNotNull(server.getTableDbName("com.triniforce.server.plugins.kernel.ServerTest.testDoDbModification2"));
            assertNotNull(server.getTableDbName("com.triniforce.server.plugins.kernel.ServerTest.testDoDbModification3"));
        }
        {   //id generator registered and work
            server.enterMode(Mode.Running);
            try{
//                IIdGenerator gen = ApiStack.getApi().getIntfImplementor(IIdGenerator.class);
//                assertNotNull(gen);
//                gen.getKey();
            } finally{
                server.leaveMode();
            }
            
        }
    }

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#registerTableDef(com.triniforce.db.ddl.TableDef)}.
     * @throws Throwable 
     */
    public void testRegisterTableDef() throws Throwable {
        TableDef def = new TableDef("com.triniforce.server.plugins.kernel.ServerTest.testRegisterTableDef");
        def.addStringField(1, "str_field", ColumnType.CHAR, 5, true, "12345");
        BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{                
                new TestPlugin(new TableDef[]{def})}));
        server.doRegistration();
        assertSame(def, server.getEntity("com.triniforce.server.plugins.kernel.ServerTest.testRegisterTableDef"));
        server.doDbModification();
        assertEquals(new ActualStateBL(getConnection()).getDBName("com.triniforce.server.plugins.kernel.ServerTest.testRegisterTableDef"), server.getTableDbName("com.triniforce.server.plugins.kernel.ServerTest.testRegisterTableDef"));
    }
    
    
    private class TestProcedure extends UpgradeProcedure{
        boolean bCalled=false;
        public TestProcedure() {
            super("test hint");
        }
        @Override
        public void run() {
            bCalled = true;
            assertNotNull(ApiStack.getApi().getIntfImplementor(ISOQuery.class));
            assertNotNull(ApiStack.getApi().getIntfImplementor(ITime.class));
            assertNotNull(ApiStack.getApi().getIntfImplementor(Connection.class));
//            assertNotNull(ApiStack.getApi().getIntfImplementor(IIdGenerator.class));
        }
    } 

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#registerUpgradeProcedure(com.triniforce.server.srvapi.UpgradeProcedure)}.
     * @throws Throwable 
     */
    public void testRegisterUpgradeProcedure() throws Throwable {
        TestProcedure proc = new TestProcedure();
        BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{new TestPlugin(new UpgradeProcedure[]{proc})}));
        server.doRegistration();
        assertSame(proc, server.getEntity(TestProcedure.class.getName()));
        server.doDbModification();

        
        proc.bCalled = false;
        server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{new TestPlugin(new UpgradeProcedure[]{proc})}));
        server.doRegistration();
        server.doDbModification();
        assertFalse(proc.bCalled);
    }

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#getEntity(java.lang.String)}.
     * @throws Throwable 
     */
    public void testISOQuery() throws Throwable {
        TestPlugin plugin = new TestPlugin(
                new IEntity[]{
                        new TableDef("com.triniforce.server.plugins.kernel.ServerTest.testGetEntityTable").addStringField(1, "f1", ColumnType.VARCHAR, 100, false, ""),
                });
        BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{plugin}));
        server.doRegistration();
        //getEntity tests
        {
            assertSame(plugin.m_defs[0], server.getEntity("com.triniforce.server.plugins.kernel.ServerTest.testGetEntityTable"));
            try{
                server.getEntity("unknownEntity");
                fail();
            } catch(ISOQuery.EServerObjectNotFound e){
                assertEquals("unknownEntity", e.getEntityName());
            }
        }
        //getEntities tests
        {
            assertTrue(server.getEntities(TableDef.class).contains(plugin.m_defs[0]));
            try{
                server.getEntities(null);
                fail();
            } catch(IllegalArgumentException e){}
        }
        // quieryEntity
        {
            assertSame(plugin.m_defs[0], server.quieryEntity("com.triniforce.server.plugins.kernel.ServerTest.testGetEntityTable"));
            assertNull(server.quieryEntity("unknownEntity"));
        }
    }

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#getTableDbName(java.lang.String)}.
     * @throws Throwable 
     */
    public void testGetTableDbName() throws Throwable {
        String entityName = "com.triniforce.server.plugins.kernel.ServerTest.testGetTableDbName";
        TableDef def = new TableDef(entityName);
        def.addStringField(1, "str_field", ColumnType.CHAR, 5, true, "12345");
        BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{                
                new TestPlugin(new TableDef[]{def})}));
        server.doRegistration();
        
        try{
            server.getTableDbName(entityName);
            fail();
        } catch(ISOQuery.EServerObjectNotFound e){
            assertEquals(entityName, e.getEntityName());
        }
        
        server.doDbModification();
        
        assertEquals(new ActualStateBL(getConnection()).getDBName(entityName), server.getTableDbName(entityName));
    }

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#currentTimeMillis()}.
     * @throws Exception 
     */
    public void testCurrentTimeMillis() throws Exception {        
        BasicServer server = new BasicServer(m_coreApi, null);
        long start = System.currentTimeMillis();
        long res = server.currentTimeMillis();
        long end = System.currentTimeMillis();
        assertTrue(start <= res && res <= end);
    }
//    
//    private static class TestDPP extends DataPreparationProcedure{
//
//        boolean bRunned = false;
//        public TestDPP() {
//            super("test dpp");
//        }
//        
//        @Override
//        public void run() throws Exception {
//            if(bRunned)
//                throw new RuntimeException();          
//            assertNotNull(ApiStack.getApi().getIntfImplementor(ISODbInfo.class));
//            bRunned = true;
//        }        
//    }

    /**
     * Test method for {@link com.triniforce.server.plugins.kernel.Server#registerDataPreparationProcedure(com.triniforce.server.srvapi.DataPreparationProcedure)}.
     * @throws Throwable 
     */
//    public void testRegisterDataPreparationProcedure() throws Throwable {
//        TestDPP dpp = new TestDPP();
//        BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{new TestPlugin(new IEntity[]{dpp})}));
//        server.doRegistration();
//        server.getEntity(dpp.getClass().getName());        
//        server.doDbModification();
//        assertTrue(dpp.bRunned);
//        BasicServer server2 = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{new TestPlugin(new IEntity[]{dpp})}));
//        server2.doRegistration();
//        server2.doDbModification();
//    }
        
    public Connection getConnect(){
        try {
            return getConnection();
        } catch (Exception e) {
            fail("connection");
        }
        return null;
    }
    
    public void testGetMode() throws Exception{
        BasicServer server = new BasicServer(m_coreApi, null);
        
        int stSz = ApiStack.getThreadApiContainer().getStack().size();
        server.enterMode(Mode.Registration);
        try{
            IServerMode srvMode = ApiStack.getApi().getIntfImplementor(IServerMode.class);
            assertEquals(Mode.Registration, srvMode.getMode());
            assertEquals(stSz, srvMode.getStackSize());
        } finally{
            server.leaveMode();
        }
    }
    
    public static class TestLogFactory extends LogFactory{
    	
    	private Log m_log;

		public TestLogFactory(Log log) {
			m_log = log;
		}

        @Override
        public Object getAttribute(String arg0) {
            fail("not implemented");
            return null;
        }

        @Override
        public String[] getAttributeNames() {
            fail("not implemented");
            return null;
        }

        @Override
        public Log getInstance(Class arg0) throws LogConfigurationException {
            return m_log;
        }

        @Override
        public Log getInstance(String arg0) throws LogConfigurationException {
            fail("not implemented");
            return null;
        }

        @Override
        public void release() {
            fail("not implemented");
        }

        @Override
        public void removeAttribute(String arg0) {
            fail("not implemented");
        }

        @Override
        public void setAttribute(String arg0, Object arg1) {
            fail("not implemented");
        }
        
    }
    
    public void testTraceStackSize() throws Exception{
        final Mockery context = new Mockery();
        Api api = new Api();
        final Log log = context.mock(Log.class);
        final LogFactory lf = new TestLogFactory(log);
        api.setIntfImplementor(LogFactory.class, lf);
        
        
        BasicServer server = new BasicServer(m_coreApi, null);
        server.doRegistration();
        if(server.isDbModificationNeeded())
        	server.doDbModification();

        final int stSz = ApiStack.getThreadApiContainer().getStack().size();
        
        server.enterMode(Mode.Running);
        try{
            ApiStack.pushApi(api);
            ApiStack.pushApi(new Api());  //<<<<-error
            ApiStack.pushApi(new Api());  //<<<<-error
            ApiStack.pushApi(new Api());  //<<<<-error
            ApiStack.pushApi(new Api());  //<<<<-error
            ApiStack.pushApi(new Api());  //<<<<-error
            
            context.checking(new Expectations(){{
            	one(log).error(with(any(String.class)), with(any(RuntimeException.class)));
            	one(log).error(with(any(String.class)), with(any(RuntimeException.class)));
                one(log).error(String.format("Api stack corrupted. Stack size: %d, must be: %d", stSz+7, stSz+1));
            }});
            server.leaveMode();
            
            context.assertIsSatisfied();
            
        }finally{
            ApiStack.popApi();
            ApiStack.popApi();
            ApiStack.popApi();
            ApiStack.popApi();
            ApiStack.popApi();
            ApiStack.popApi();            
        }
    }
    
    public void testEnterMode() throws Exception{
        {
            BasicServer server = new BasicServer(m_coreApi, null);            
            try{
                server.enterMode(Mode.Running);
                fail();
            } catch (EInvalidServerState e){
                assertEquals(Mode.Running, e.getState());
            }
        }
        {
            TableDef def = new TableDef("com.triniforce.server.plugins.kernel.ServerTest.testEnterMode");
            def.addStringField(1, "strField", ColumnType.CHAR, 5, true, "12345");
            BasicServer server = new BasicServer(m_coreApi, Arrays.asList(new IPlugin[]{                
                    new TestPlugin(new TableDef[]{def})}));
            server.doRegistration();
            assertTrue(server.isDbModificationNeeded());
            try{
                server.enterMode(Mode.Running);
                fail();
            } catch (BasicServer.EInvalidServerState e){}
        }
        {
            BasicServer server = new BasicServer(m_coreApi, null);
            server.doRegistration();
            assertTrue(server.isDbModificationNeeded());
            server.doDbModification();
            server.enterMode(Mode.Running);
//            ApiStack.getApi().getIntfImplementor(IIdGenerator.class);
//            ApiStack.getApi().getIntfImplementor(IServerBundle.class);
//            ApiStack.getApi().getIntfImplementor(IServerBundleCache.class);
            ISrvSmartTranFactory trnFact = ApiStack.getApi().getIntfImplementor(ISrvSmartTranFactory.class);
            trnFact.pop();
            assertNotNull(ApiStack.getApi().queryIntfImplementor(IServerMode.class));
            assertNull(ApiStack.getApi().queryIntfImplementor(Connection.class));
//            assertNull(ApiStack.getApi().queryIntfImplementor(ISrvSmartTran.class));
//            assertNotNull(ApiStack.getApi().queryIntfImplementor(IFlatViewStorage.class));
            IDatabaseInfo dbInfo = ApiStack.getInterface(IDatabaseInfo.class);
            assertNotNull(dbInfo);
            assertEquals(getDbType(), dbInfo.getDbType());
            if(DbType.MYSQL.equals(dbInfo.getDbType())){
            	assertEquals("`", dbInfo.getIdentifierQuoteString());	
            }
            else
            	assertEquals("\"", dbInfo.getIdentifierQuoteString());
            trnFact.push();
            server.leaveMode();
        }
        {
            BasicServer server = new BasicServer(m_coreApi, null);
            server.doRegistration();
            assertTrue(server.isDbModificationNeeded());
            server.enterMode(Mode.Upgrade);
            try{
//                ApiStack.getApi().getIntfImplementor(IServerBundle.class);
//                ApiStack.getApi().getIntfImplementor(IServerBundleCache.class);
            }finally{
                server.leaveMode();
            }
        }
    }
    
    public void testLeaveMode() throws Exception{
    	BasicServer srv = new BasicServer(m_coreApi);
    	srv.doRegistration();
    	srv.doDbModification();
    	
    	int sz = ApiStack.getThreadApiContainer().getStack().size();
    	
    	srv.enterMode(Mode.Running);
    	assertNotNull(SrvApiAlgs2.getIServerTran());
    	assertNotNull(SrvApiAlgs2.getISrvTranFactory());
    	srv.leaveMode();
    	
    	assertEquals(sz, ApiStack.getThreadApiContainer().getStack().size());
    	//assertNull(ApiStack.queryInterface(IPooledConnection.class));
    }
    
    @SuppressWarnings("deprecation")
    public void testFinit() throws Exception{
    	{
	    	BasicServer srv = new BasicServer(m_coreApi);
	    	
	    	srv.doRegistration();
	    	srv.doDbModification();
	    	
	    	Mockery ctx = new Mockery();
	    	final IPlugin plg = ctx.mock(IPlugin.class);
	    	srv.addPlugin(plg);
	    	srv.addPlugin(plg);
	    	
	    	final Log log = ctx.mock(Log.class);
	    	Api api = new Api();
	    	api.setIntfImplementor(LogFactory.class, new TestLogFactory(log));
	    	ApiStack.pushApi(api);
	    	try{
		    	
		    	ctx.checking(new Expectations(){{
		    		RuntimeException e = new RuntimeException();
		    		exactly(6).of(plg).pushApi(with(any(Mode.class)), with(any(ApiStack.class)));
		    		one(plg).finit(); will(throwException(e));
		    		one(plg).finit();
		    		exactly(6).of(plg).popApi(with(any(Mode.class)), with(any(ApiStack.class)));
		    		one(log).error(with(any(String.class)), with(any(RuntimeException.class)));
		        }});
		    	
		    	srv.finit();
		    	
		    	ctx.assertIsSatisfied();
	    	} finally{
	    		ApiStack.popApi();
	    	}
    	}
    	{
	    	BasicServer srv = new BasicServer(m_coreApi);
	    	srv.doRegistration();
	    	if(srv.isDbModificationNeeded())
	    		srv.doDbModification();
    		srv.finit();
    	}
    }
    
}
