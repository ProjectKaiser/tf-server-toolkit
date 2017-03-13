/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.db.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.server.plugins.kernel.IdDef;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.TFUtils;

import junit.framework.TestCase;
import net.sf.sojo.common.CompareResult;
import net.sf.sojo.common.ObjectUtil;

/**
 * Installs ILogger interface
 */
public class TFTestCase extends TestCase {

    public final static String TF_TEST_FOLDER_EX = "TRINIFORCE_TEST_FOLDER";
    public final static String TOOLKIT_TEST_FOLDER = "TF_SERVER_TOOLKIT_TEST_FOLDER";
    
    public void copyTestResources(String resources[], File dest){
        try {
            for (String resource : resources) {
                InputStream is = this.getClass()
                        .getResourceAsStream(resource);
                OutputStream os = new FileOutputStream(new File(dest, resource));
                TFUtils.copyStream(is, os);
                is.close();
                os.close();
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }        
    }
    
    public String uuid(String prefix){
    	return prefix + "_" + UUID.randomUUID().toString();
    }
    
    public static String getTfTestFolder(){
        String res = System.getenv(TOOLKIT_TEST_FOLDER);
        Object res2 = System.getenv(TFTestCase.TF_TEST_FOLDER_EX);
        assertNotNull("Environment variable " + TOOLKIT_TEST_FOLDER +  " is not set" + (res2==null?"":". Rename " +TF_TEST_FOLDER_EX+" variable"), res);
    	return res;
    }
    
    /**
     * Use getTempTestFolder() or getTmpFolder(TFTestCase test) instead
     * @return
     */
    @Deprecated
    public static File getTmpFolder(){
        File res = new File(getTfTestFolder(), "tmp");
        res.mkdirs();
        return res;
    }
    
    public File getTempTestFolder(){
        return getTmpFolder(this);
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
    
    static boolean bInitLogFile = true;
    public static boolean bWriteToInitLogFile = true;
    
    static File TEST_LOG_FILE = null;

    public static String getTestPropFile() {
        return new File(TFTestCase.getTfTestFolder(),
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
    
    public static String getMustHaveTestProperty(String key) {
        String res = getTestProperties().getProperty(key);
        TFUtils.assertNotNull(res, "tes.properties must have property:" + key);
        return res;
    }
    
    public static String getTestProperty(String key, String def) {
        return getTestProperties().getProperty(key, def);
    }

	private LogFactory m_testLF;    

    protected int m_apiStackCnt;
    
	private static ErrorCounter errCounter;
    
    protected static boolean log4jConfigured = false;
    
    public class ErrorCounter extends AppenderSkeleton{
        protected int m_errorCnt = 0;
        protected boolean bCountErrors = true;


		@Override
		public void close() {
		}

		@Override
		public boolean requiresLayout() {
			return false;
		}

		@Override
		protected void append(LoggingEvent event) {
			if(event.getLevel().isGreaterOrEqual(Level.ERROR) && bCountErrors){
				m_errorCnt ++;
			}
		}
    	
    } 
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
//        if(! log4jConfigured){
            PropertyConfigurator.configure(new File(TFTestCase.getTfTestFolder(), LOG4J_FILE).toString());
            
            errCounter = new ErrorCounter();
            Logger.getRootLogger().addAppender(errCounter);
            log4jConfigured = true;
//        }
        
        errCounter.m_errorCnt = 0;
        errCounter.bCountErrors = true;
        
        m_apiStackCnt = ApiStack.getThreadApiContainer().getStack().size();
        Api api = new Api();
//        m_testLF = new TestLogFactory();
        m_testLF = LogFactory.getFactory();
        api.setIntfImplementor(LogFactory.class, m_testLF);
        api.setIntfImplementor(IDatabaseInfo.class, DBTestCase.getDbInfo());
        api.setIntfImplementor(IIdDef.class, new IdDef(ColumnType.LONG));
        ApiStack.pushApi(api);
        m_expectedLogErrorCount = 0;
        Thread.currentThread().setName(this.getClass().getSimpleName());
        
        File testLog;
        if(bWriteToInitLogFile &&  null != (testLog = getTestsLogFile())){
        	FileOutputStream out = new FileOutputStream(testLog, true);
        	out.write(String.format("%s %s\n", getClass().getName(), getName()).getBytes("utf-8"));
        	out.flush();
        	out.close();
        }
    }

    public File getTestsLogFile() {
		if(bInitLogFile){
			String fname = getTestProperty("testSeqFile");
			if(null != fname){
				TEST_LOG_FILE = new File(TFTestCase.getTfTestFolder(), fname);
				try {
					TEST_LOG_FILE.createNewFile();
				} catch (IOException e) {
					ApiAlgs.rethrowException(e);
				}
			}
			bInitLogFile = false;
		}
		return TEST_LOG_FILE;
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
    
    @Deprecated
    public void avoidTempTestFolderDeletion(){        
        avoidTmpFolderDeletion();        
    }
    
    protected void checkResources(){
        assertEquals("ApiStack damaged", m_apiStackCnt, ApiStack.getThreadApiContainer().getStack()
                .size());        
        assertEquals("log errors in test", m_expectedLogErrorCount, getErrorCount());        
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

    
    public void assertObjectsEqual(Object expected, Object actual){
    	ObjectUtil ou = new ObjectUtil();
    	CompareResult cr = ou.compare(expected, actual);
    	if(null != cr){
    		TFUtils.assertTrue(false, cr.toString());
    	}
    }
    
    public void trace(Object obj) {
        ApiAlgs.getLog(this).trace(obj);
    }
    
    @Deprecated
    public void dtrace(Object obj){
        ApiAlgs.getDevLog(this).trace(obj);
    }

    public void trace(String msg, Object... args) {
        trace(String.format(msg, args));
    }

	public void countErrorLogs(boolean bCount) {
		errCounter.bCountErrors = bCount;
	}

	public boolean isCountErrorLogs() {
		return errCounter.bCountErrors;
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
	
	
	public static void closeDataSource() throws SQLException {
		if(null != DATA_SOURCE){
			DATA_SOURCE.close();
			DATA_SOURCE = null;
		}
	}

	public int getErrorCount() {
		return errCounter.m_errorCnt;
	}

}
