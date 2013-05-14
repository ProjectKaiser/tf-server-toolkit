/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.triniforce.db.test.TFTestCase;

public class TFUtilsLogsTest extends TFTestCase {
    
    public void testGetTailAndShitHours(){
        File tmp = getTmpFolder(this);
        File log = new File(tmp, "log");
        log.delete();
    }
    
    public void testConfigureLog() throws IOException, InterruptedException{
        try{
            TFUtilsLogs.configureLog("", this.getClass(), "");
            fail();
        } catch(RuntimeException e){}
        
        File fTest = getTmpFolder(this);
        {
            TFUtils.delTree(fTest, true);
            assertTrue(fTest.mkdir());
        }
        {
            File f = new File(fTest, "pp/logs01");
            String folder = f.getAbsolutePath();
                    
            trace(folder);
            TFUtilsLogs.configureLog(folder, this.getClass(), "test_log4j.properties");
            
            assertTrue(new File(folder).exists());
            
            TFUtilsLogs.configureLog(folder, this.getClass(), "test_log4j.properties");
            
            File flog = new File(folder, "log4j.properties");
            assertTrue(flog.exists());
            
            
            Log log = LogFactory.getLog(getClass());
            log.trace("test_log_string");
            
            FileReader fr = new FileReader(flog);
            BufferedReader br = new BufferedReader(fr);
            assertEquals("log4j.appender.stdout=org.apache.log4j.ConsoleAppender", br.readLine());
            fr.close();
            
            Thread.sleep(1000L);
            
            fr = new FileReader(new File(folder, "test-messages.log"));
            br = new BufferedReader(fr);
            assertEquals("TST_01TRACE-test_log_string", br.readLine());
            fr.close();
        }
        
        {
            File f = new File(fTest, "logs02");
            String folder = f.getAbsolutePath();
            TFUtilsLogs.configureLog(folder, this.getClass(), "test02_log4j.properties"); // first used
            TFUtilsLogs.configureLog(folder, this.getClass(), "test_log4j.properties");

            Log log = LogFactory.getLog(getClass());
            log.trace("test_log_string_check_first_file_used");
            
            FileReader fr = new FileReader(new File(folder, "test02-messages.log"));
            BufferedReader br = new BufferedReader(fr);
            assertEquals("TST_02TRACE-test_log_string_check_first_file_used", br.readLine());
            fr.close();
        }
        { // Folder exists but there is no log4j
            File f = new File(fTest, "pp/logs01");
            String folder = f.getAbsolutePath();
            File flog = new File(f, "log4j.properties");
            assertTrue(flog.delete());
            TFUtilsLogs.configureLog(folder, this.getClass(), "test_log4j.properties");
        }
    }

}
