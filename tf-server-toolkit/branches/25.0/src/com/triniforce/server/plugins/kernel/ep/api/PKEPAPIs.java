/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.api;

import com.triniforce.extensions.PKExtensionPoint;

public class PKEPAPIs  extends PKExtensionPoint{
    public PKEPAPIs() {
        setExtensionClass(IPKEPAPI.class);
        setSingleExtensionInstances(true);
    }

}
