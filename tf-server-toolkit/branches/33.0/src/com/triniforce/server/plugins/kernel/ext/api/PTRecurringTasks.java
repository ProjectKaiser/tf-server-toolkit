/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ext.api;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIPeriodicalTask;
import com.triniforce.server.plugins.kernel.recurring.PKEPRecurringTasks;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ITime;

public class PTRecurringTasks  extends PKEPAPIPeriodicalTask implements IPKEPAPI{

    public PTRecurringTasks() {
    }
    
    public Class getImplementedInterface() {
        return PTRecurringTasks.class;
    }

    @Override
    public void run() {
        IBasicServer bs = ApiStack.getInterface(IBasicServer.class);
        PKEPRecurringTasks rts = (PKEPRecurringTasks) bs.getExtensionPoint(PKEPRecurringTasks.class);
        rts.processTasksInTransactions(ITime.ITimeHelper.currentTimeMillis());
    }

}
