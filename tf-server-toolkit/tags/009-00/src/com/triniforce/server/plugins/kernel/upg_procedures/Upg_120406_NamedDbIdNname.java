/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.upg_procedures;

import com.triniforce.server.plugins.kernel.tables.TNamedDbId;
import com.triniforce.server.srvapi.UpgradeProcedure;

public class Upg_120406_NamedDbIdNname  extends UpgradeProcedure {
    @Override
    public void run() throws Exception {
        TNamedDbId.updateNname();
    }

}
