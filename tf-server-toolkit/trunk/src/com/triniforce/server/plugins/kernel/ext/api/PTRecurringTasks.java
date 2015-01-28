/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.api;

import com.triniforce.qsync.impl.QSyncManager;
import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIPeriodicalTask;
import com.triniforce.utils.ApiStack;

public class PTRecurringTasks  extends PKEPAPIPeriodicalTask implements IPKEPAPI{

    public PTRecurringTasks() {
        delay = 60*1000; // 1 Minute
        initialDelay = 0;

    }
    
    public Class getImplementedInterface() {
        return PTRecurringTasks.class;
    }

    @Override
    public void run() {
        QSyncManager sm = ApiStack.getInterface(QSyncManager.class);
        sm.onEveryMinute();
    }

}
