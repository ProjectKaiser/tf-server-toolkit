/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.plugins.kernel.ep.srv_ev.PKEPServerEvents;
import com.triniforce.server.srvapi.ITaskExecutors;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.ITimedLock2;
import com.triniforce.utils.ApiStack;

public class BasicServerCorePluginTest extends BasicServerTestCase {
    
    @Override
    public void test() throws Exception {
        //test interfaces
        getServer().enterMode(Mode.Running);
        try{
            ApiStack.getInterface(ITaskExecutors.class);
            ApiStack.getInterface(ITimedLock2.class);
            getServer().getExtensionPoint(PKEPServerEvents.class);
        }finally{
            getServer().leaveMode();
        }
        

    }

}
