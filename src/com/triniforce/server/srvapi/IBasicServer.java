/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.srvapi;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import com.triniforce.db.ddl.TableDef.EReferenceError;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.server.plugins.kernel.BasicServer.ServerException;
import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor.BasicPeriodicalTask;
import com.triniforce.server.plugins.kernel.services.IService;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public interface IBasicServer extends IPKRootExtensionPoint {
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
    
    public void doPluginsRegistration();
    
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
    public boolean isRegistered();
    
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
    
    /**
     * Invokes stopPeriodicalTasks() then Plugin.finit();
     */
    void finit();
    
    List<IPlugin> getPlugins();
    
    void addPeriodicalTask(BasicPeriodicalTask ptask);
    List<BasicPeriodicalTask> getPeriodicalTasks();
    
    void startPeriodicalTasks();
    void stopPeriodicalTasks();
    
    /**
     * executeBeanShell(script, true)
     */
    boolean executeBeanShell(File script);

    /**
     * Plays given file in BeanShell if it exists, catch all exceptions and writes them into log. Return false if any exception occurs.
     * <p>If executeBeanShell then script executes <tt>enterMode(Mode.Running)</tt>, otherwise IBasicServer is pushed into ApiStack.
     */
    boolean executeBeanShell(File script, boolean enterRunningMode);
    
    /**
     * Must be called after doDbModification() before startPeriodicalTasks();
     */
    void init();
    
    ApiStack getCoreApi();
    
    void setBaseApi(Api baseApi);
    
    void initAndStart();
    /**
     * Does NOT throw any exception
     */
    void stopAndFinit();
 
    
    
    /**
     * Start services asyncronouosly. Method returns when ServiceManager is started.
     */
    void startServices();
    
    /**
     * Initiate process of stopping all services and return. If startServices was called before
     * all servces will be started then stopped.
     */
    void stopServices();
    
    void stopServicesAndWait();
    
    /**
     * @param id
     * @return
     */
    IService.State queryServiceState(long id);
 
    ScheduledFuture<?> getPeriodicalTaskFeature(Class<?> clsTask);
}


