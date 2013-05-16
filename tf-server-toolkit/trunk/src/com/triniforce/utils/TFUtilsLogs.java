/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TFUtilsLogs{
    
    static final String LOG4J_CONFIG_FILE = "log4j.properties"; 
    static final String FOLDER_TAG = "\\$\\{catalina\\.home\\}";

    /**
     * Investigate how to configure logs
     * http://www.projectkaiser.com/online?fileid=3026218
     * 
     * Function configure Log4j.properties file
     * @param logFolder - folder where log should be, create and place it there if file/folder is not found
     * @param log4JResource - resource containing log4j.properties template
     * @throws IOException
     */
    public static void configureLog(String logFolder, Class resourceClass, String resourceName){
        File f = new File(logFolder);
        File flog;
        flog = new File(f, LOG4J_CONFIG_FILE);
        if(!f.exists()){
            if(!f.mkdirs())
                throw new RuntimeException("Couldn't create " + f);
        }
        if (!flog.exists()){
            String lf = logFolder.replace("\\", "/");
            String props = TFUtils.readResource(resourceClass, resourceName).replaceAll(FOLDER_TAG, lf);
            TFUtils.printlnToFile(flog, props);
        }
        PropertyConfigurator.configure(flog.getAbsolutePath());
    }
    
    public String readTailAndShiftHours(File f, int numOfStrings, int numOfHours){
        return "";
    }
    
    static final Pattern dtPattern = Pattern.compile("(\\s)(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)");
    static final DateTimeFormatter dtFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    public static String addHoursToPrintedDateTime(String src, int hoursToAdd){
        Matcher m = dtPattern.matcher(src);
        while (m.find()){
            int year = Integer.parseInt(m.group(2));
            int month = Integer.parseInt(m.group(3));
            int day = Integer.parseInt(m.group(4));
            int hour = Integer.parseInt(m.group(5));
            int minute = Integer.parseInt(m.group(6));
            int sec = Integer.parseInt(m.group(7));
            DateTime start = new DateTime(year, month, day, hour, minute, sec);
            
            String newDateStr = m.group(1) + start.plusHours(hoursToAdd).toString(dtFmt);
            return  m.replaceFirst(newDateStr);
        }
        return src;
    }
    
}
