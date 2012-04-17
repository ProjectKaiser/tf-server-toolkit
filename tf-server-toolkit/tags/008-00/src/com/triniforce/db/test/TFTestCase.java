/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.db.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.server.plugins.kernel.IdDef;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.TFUtils;

/**
 * Installs ILogger interface
 */
public class TFTestCase extends TestCase {
	
	public static class TestLogFactory extends org.apache.commons.logging.impl.LogFactoryImpl{

		private int m_errCount=0;
		public boolean bCountErrors=true;

		public int getErrorCount() {
			return m_errCount;
		}
		
//		static class TestLog extends org.apache.commons.logging.impl.Log4JLogger{
//			private static final long serialVersionUID = 1L;
//			private TestLogFactory m_lf;
//
//			public TestLog(TestLogFactory lf, org.apache.log4j.Logger inner) {
//				super(inner);
//				m_lf = lf;
//			}
//			
//			@Override
//			public void error(Object arg0) {
//				if(m_lf.bCountErrors){
//					m_lf.m_errCount++;
//					super.error(arg0);
//				}
//				else
//					trace(arg0);
//			}
//			
//			@Override
//			public void error(Object arg0, Throwable arg1) {
//				if(m_lf.bCountErrors){
//					m_lf.m_errCount++;
//					super.error(arg0, arg1);
//				}
//				else 
//					trace(arg0, arg1);
//			}
//		}
//		
		
		public static class TestLog implements Log {

			private TestLogFactory m_lf;
			private Log m_log;

			public TestLog(TestLogFactory factory, Log log) {
				m_log = log;
				m_lf = factory;
			}

			public void debug(Object arg0) {
				m_log.debug(arg0);
			}

			public void debug(Object arg0, Throwable arg1) {
				m_log.debug(arg0, arg1);
			}

			public void error(Object arg0) {
				if(m_lf.bCountErrors){
					m_lf.m_errCount++;
					m_log.error(arg0);
				}
				else
					trace(arg0);
			}
			
			public void error(Object arg0, Throwable arg1) {
				if(m_lf.bCountErrors){
					m_lf.m_errCount++;
					m_log.error(arg0, arg1);
				}
				else 
					trace(arg0, arg1);
			}

			public void fatal(Object arg0) {
				m_log.fatal(arg0);
			}

			public void fatal(Object arg0, Throwable arg1) {
				m_log.fatal(arg0, arg1);
			}

			public void info(Object arg0) {
				m_log.info(arg0);
			}

			public void info(Object arg0, Throwable arg1) {
				m_log.info(arg0, arg1);
			}

			public boolean isDebugEnabled() {
				return m_log.isDebugEnabled();
			}

			public boolean isErrorEnabled() {
				return m_log.isErrorEnabled();
			}

			public boolean isFatalEnabled() {
				return m_log.isFatalEnabled();
			}

			public boolean isInfoEnabled() {
				return m_log.isInfoEnabled();
			}

			public boolean isTraceEnabled() {
				return m_log.isTraceEnabled();
			}

			public boolean isWarnEnabled() {
				return m_log.isWarnEnabled();
			}

			public void trace(Object arg0) {
				m_log.trace(arg0);
			}

			public void trace(Object arg0, Throwable arg1) {
				m_log.trace(arg0, arg1);
			}

			public void warn(Object arg0) {
				m_log.warn(arg0);
			}

			public void warn(Object arg0, Throwable arg1) {
				m_log.warn(arg0, arg1);
			}

		}
		
		@Override
		public Log getInstance(String arg0) throws LogConfigurationException {
			return new TestLog(this, super.getInstance(arg0));
//			Log4JLogger inner = (Log4JLogger) super.getInstance(arg0);
//			return new TestLog(this, inner.getLogger());
		}
		
	}

    public final static String TF_TEST_FOLDER = "TRINIFORCE_TEST_FOLDER";
    
    public static String getTfTestFolder(){
    	return System.getenv(TFTestCase.TF_TEST_FOLDER);
    }
    
    public static File getTmpFolder(){
        File res = new File(System.getenv(TFTestCase.TF_TEST_FOLDER), "tmp");
        res.mkdirs();
        return res;
    }
    
    boolean tmpFolderCreated = false;
    public static File getTmpFolder(TFTestCase test){
        File tmp = new File(getTmpFolder(), test.getClass().getCanonicalName());
        if(test.tmpFolderCreated){
            return tmp;
        }
        TFUtils.delTree(tmp, false);
        tmp.mkdirs();
        test.tmpFolderCreated = true;
        return tmp;
    }
    
    public static File getTmpFolder(TFTestCase test, String subtest){
        File tmp = new File(getTmpFolder(test), subtest);
        tmp.mkdirs();
        return tmp;
    }

    public final static String DB_CONNECTION_FILE = "test.properties";

    public final static String LOG4J_FILE = "/log4j.properties";

    static Properties m_props;
    
    public static BasicDataSource DATA_SOURCE = null;

    public static String getTestPropFile() {
        return new File(System.getenv(TFTestCase.TF_TEST_FOLDER),
                TFTestCase.DB_CONNECTION_FILE).toString();
    }

    public static Properties getTestProperties(){
        if (null == m_props) {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream(getTestPropFile()));
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
            m_props = props;
        }
        return m_props;        
    }
    
    public static String getTestProperty(String key) {
        return getTestProperties().getProperty(key);
    }
    public static String getTestProperty(String key, String def) {
        return getTestProperties().getProperty(key, def);
    }

	private TestLogFactory m_testLF;    

    {
        PropertyConfigurator.configure(new File(System.getenv(TF_TEST_FOLDER),
                LOG4J_FILE).toString());
    }
    
    protected int m_apiStackCnt;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_apiStackCnt = ApiStack.getThreadApiContainer().getStack().size();
        Api api = new Api();
        m_testLF = new TestLogFactory();
        api.setIntfImplementor(LogFactory.class, m_testLF);
        api.setIntfImplementor(IDatabaseInfo.class, DBTestCase.getDbInfo());
        api.setIntfImplementor(IIdDef.class, new IdDef(ColumnType.LONG));
        ApiStack.pushApi(api);
        m_expectedLogErrorCount = 0;
        Thread.currentThread().setName(this.getClass().getSimpleName());        
    }

    private int m_expectedLogErrorCount;
    
    public void incExpectedLogErrorCount(int value){
		m_expectedLogErrorCount += value;
    }
    
    boolean m_avoidTmpFolderDeletion = false;
    
    @Deprecated
    public void avoidTmpFolderDeletion(){        
        m_avoidTmpFolderDeletion = true;        
    }
    
    protected void checkResources(){
        assertEquals("ApiStack damaged", m_apiStackCnt, ApiStack.getThreadApiContainer().getStack()
                .size());        
        assertEquals("log errors in test", m_expectedLogErrorCount, m_testLF.getErrorCount());        
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(tmpFolderCreated && !m_avoidTmpFolderDeletion ){
            TFUtils.delTree(getTmpFolder(this), true);
        }
        ApiStack.popApi();
        checkResources();
        super.tearDown();        
    }

    public void test() throws Exception {
    }

    public void trace(Object obj) {
        ApiAlgs.getLog(this).trace(obj);
    }

    public void trace(String msg, Object... args) {
        trace(String.format(msg, args));
    }

	public void countErrorLogs(boolean bCount) {
		m_testLF.bCountErrors = bCount;
	}
	
	public static final BasicDataSource getDataSource(){
		if(null == DATA_SOURCE){
	        DATA_SOURCE = new BasicDataSource();
	
	        DATA_SOURCE.setDriverClassName(getTestProperty("class"));
	        DATA_SOURCE.setUrl(getTestProperty("url"));
	        //ds.setMaxOpenPreparedStatements(200);
	        DATA_SOURCE.setPoolPreparedStatements(true);
	        String dbUserName = getTestProperty("userName");
	        if(null != dbUserName)
	        	DATA_SOURCE.setUsername(dbUserName);
	        String dbPassword = getTestProperty("password");
	        if(null != dbPassword)
	        	DATA_SOURCE.setPassword(dbPassword);
		}
		return DATA_SOURCE;
	}

}
