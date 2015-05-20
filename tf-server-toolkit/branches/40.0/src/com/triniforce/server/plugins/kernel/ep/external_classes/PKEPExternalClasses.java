/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import com.triniforce.extensions.PKExtensionPoint;

public class PKEPExternalClasses extends PKExtensionPoint {
    public PKEPExternalClasses() {
        setSingleExtensionInstances(true);
        setExtensionClass(ClassesFolder.class);
    }

}
