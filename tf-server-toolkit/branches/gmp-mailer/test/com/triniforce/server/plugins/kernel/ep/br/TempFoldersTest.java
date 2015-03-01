/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;

import com.triniforce.db.test.TFTestCase;

public class TempFoldersTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        TempFolders tf = new TempFolders();
        File f1 = tf.getTempFolder();
        File f2 = tf.getTempFolder();
        assertTrue(f1.exists());
        assertTrue(f1.isDirectory());
        assertTrue(f2.exists());
        assertTrue(f2.isDirectory());
        tf.finit();
        assertFalse(f1.exists());
        assertFalse(f2.exists());
    }

}
