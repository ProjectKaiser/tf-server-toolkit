/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.srvapi;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import com.triniforce.db.ddl.TableDef.EReferenceError;
import com.triniforce.server.plugins.kernel.BasicServer.ServerException;
import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor.BasicPeriodicalTask;

public interface IBasicServer {
    void enterMode(final Mode mode);
    void leaveMode();
    
    /**
     * Server modes:
     * <p>
     * 
     * <li>Registration. Plugins register server objects
     * <li>Upgrade. Server executes data upgrade procedures
     * <li>Running. Server accepts user requests
     * <p>
     * 
     */
    public enum Mode {
        None, Registration, Upgrade, Running
    }
    
    /**
     * Register server. Run doRegistration for all plugin. Calculate
     * isDbModification() flag
     * 
     * @throws ServerException
     * @throws SQLException
     */
    public void doRegistration() throws ServerException, SQLException;
    
    /**
     * @return true if database modification needed
     * @throws ServerException
     *             if server is not registered
     * @throws EReferenceError
     *             something wrong in table referenes
     */
    public boolean isDbModificationNeeded() throws ServerException, EReferenceError;
    
    /**
     * Run database modification first run table schemas modification
     * 
     * @throws EReferenceError
     *             something wrong in desired table state
     * @throws ServerException
     * @throws SQLException
     *             database error
     * @throws ClassNotFoundException
     */
    public void doDbModification() throws Exception;
    
    void finit();
    
    List<IPlugin> getPlugins();
    
    void addPeriodicalTask(BasicPeriodicalTask ptask);
    List<BasicPeriodicalTask> getPeriodicalTasks();
    
    void startPeriodicalTasks();
    void stopPeriodicalTasks();
    
    /**
     * Plays given file in BeanShell if it exists, catch all exceptions and writes them into log. Return false if any exception occurs.
     */
    boolean executeBeanShell(File script);
    
    /**
     * Must be called after doDbModification() before startPeriodicalTasks();
     */
    void init();
   
}


