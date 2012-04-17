/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import com.triniforce.db.test.TFTestCase;

public class IPKRootExtensionPointTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        PKRootExtensionPoint rep = new PKRootExtensionPoint();
        
        assertTrue(rep.getExtensionPoint(PKEPFunctions.class) instanceof PKEPFunctions);
        assertTrue(rep.getExtensionPoint(PKEPPlugins.class) instanceof PKEPPlugins);
    }
    

}
