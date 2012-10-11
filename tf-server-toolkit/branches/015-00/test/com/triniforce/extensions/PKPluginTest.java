/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import com.triniforce.db.test.TFTestCase;

public class PKPluginTest extends TFTestCase {
    
    static class MyPlugin extends PKPlugin{
        @Override
        public void doExtensionPointsRegistration() {
        }

        @Override
        public void doRegistration() {
        }
    }
    
    @Override
    public void test() throws Exception {
        PKPlugin p = new MyPlugin();
        assertEquals("", p.getVersion());

    }

}
