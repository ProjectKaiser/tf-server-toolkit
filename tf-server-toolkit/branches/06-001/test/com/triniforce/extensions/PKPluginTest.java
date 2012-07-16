/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import com.triniforce.db.test.TFTestCase;

public class PKPluginTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        PKPlugin p = new PKPlugin();
        assertEquals("", p.getVersion());

    }

}
