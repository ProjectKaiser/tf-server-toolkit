/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.server.srvapi;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.utils.ApiStack;

/**
 * Interface to be realized by plugin
 * 
 */
public interface IPlugin {


    /**
     * ServerCorePlugin performs registration task using ISORegistration
     * interface. reg is interface currently installed in SrvSpi.
     * 
     * @throws ED,
     *             BObjectExceptio
     * @throws FileDefException
     *             nFileDefException
     * 
     */
    public void doRegistration(ISORegistration reg) throws EDBObjectException;

    /**
     * @return like "Triniforce Server Core Plug-in"
     */
    public String getPluginName();

    /**
     * @return like "Triniforce.com"
     */
    public String getProviderName();

    /**
     * @return ID's of plug-in this plug-in depends on, empty [] if none
     */
    public String[] getDependencies();

    /**
     * Called once after doRegistration() is called for all plugins
     */
    public void prepareApi();

    /**
     * Called each time server enters given mode
     */
    void pushApi(final IBasicServer.Mode mode, ApiStack apiStack);

    /**
     * Called each time server leaves given mode
     * 
     * @param stk
     */
    void popApi(final IBasicServer.Mode mode, ApiStack stk);

    public void finit();
    /**
     * Called after doRegistration and prepareApi. Db is initialized
     */
    public void init();

}
