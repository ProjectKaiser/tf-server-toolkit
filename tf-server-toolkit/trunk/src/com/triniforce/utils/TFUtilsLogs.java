/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;



public class TFUtilsLogs {
    
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
    
    
    
}
