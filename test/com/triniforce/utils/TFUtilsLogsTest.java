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

    public void test_addMinutesToPrintedDateTime(){
        assertEquals("qqq", TFUtilsLogs.addHoursToPrintedDateTime("qqq", 2));
        
        //no changes since no whitespace in the beg
        assertEquals("=2012-12-06 17:57:28", TFUtilsLogs.addHoursToPrintedDateTime("=2012-12-06 17:57:28", 1));
        
        //shift one hour
        assertEquals(" 2012-12-06 18:57:28", TFUtilsLogs.addHoursToPrintedDateTime(" 2012-12-06 17:57:28", 1));
        //tabulation + shift one hour
        assertEquals("\t2012-12-06 18:57:28", TFUtilsLogs.addHoursToPrintedDateTime("\t2012-12-06 17:57:28", 1));
        
        //tabulation + shift to next day
        assertEquals("\t2012-12-07 00:57:28", TFUtilsLogs.addHoursToPrintedDateTime("\t2012-12-06 17:57:28", 7));
        
        //second date intact
        
        assertEquals("\t2012-12-07 00:57:28\t2012-12-06 17:57:28", TFUtilsLogs.addHoursToPrintedDateTime("\t2012-12-06 17:57:28\t2012-12-06 17:57:28", 7));
        
        //shift -10 minutes
        assertEquals(" 2012-12-06 17:47:28", TFUtilsLogs.addMinutesToPrintedDateTime(" 2012-12-06 17:57:28", -10));        
        
    }
    
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
        log4jConfigured = false;
        
        File fTest = getTmpFolder(this, "testConfigureLog");
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
