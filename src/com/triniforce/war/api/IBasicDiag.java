/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war.api;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;

public interface IBasicDiag extends IPKEPAPI{

    public static final String ERROR_LOG_NAME = "errors.pk.log";
    public static final String MESSAGES_LOG_NAME = "messages.pk.log";
    /**
     * Returns last nlines from file with given name, separated by "\n"
     * 
     */        
    public String getTextFile(String fileName, int nlines);
    
    /**
     * Returns last nlines from cataline.home/logs/logName
     * <p>Example:
     * <p>
     * <code>
     * getCatalinaLog("errors.pk.log", 1000)
     * <br>
     * getCatalinaLog("messages.pk.log", 1000)
     * </code>
     * 
     */        
    public String getCatalinaLog(String logName, int nlines);
    
    /**
     * 
     * Returns some information about current environment
     *  
     */        
    public String getEnvironment();
    
    /**
     * <p>Select all deactivated files:
     * <p>
     * <code>
     * diag.executeSelect("select id, name from t_tfile where is_active <> 1", 10)
     * </code>
     */
    public String executeSelect(String sql, int limit);
    public int executeUpdate(String sql);
    
}