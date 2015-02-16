/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.view;

import com.triniforce.dbo.datasets.FieldFunction;
import com.triniforce.extensions.PKExtensionPoint;

public class PKEPFieldFunctions extends PKExtensionPoint{
    public PKEPFieldFunctions() {
        setExtensionClass(FieldFunction.class);
        setSingleExtensionInstances(false);
    }
}
