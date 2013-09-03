/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.tr_ext;

import com.triniforce.extensions.PKExtensionPoint;
import com.triniforce.server.srvapi.ISrvSmartTranFactory.ITranExtender;

public class PKEPTranInners extends PKExtensionPoint{
    public PKEPTranInners() {
        setSingleExtensionInstances(true);
        setExtensionClass(ITranExtender.class);
    }
    
    @Override
    public String getWikiDescription() {
        return "Transaction Inners";
    }    

}
