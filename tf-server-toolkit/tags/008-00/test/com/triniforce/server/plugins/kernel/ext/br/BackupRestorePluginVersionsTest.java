/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ext.br;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.plugins.kernel.ep.br.PKEPBackupRestore;

public class BackupRestorePluginVersionsTest extends BasicServerTestCase {
    @Override
    public void test() throws Exception {
        PKEPBackupRestore br = (PKEPBackupRestore) getServer()
                .getExtensionPoint(PKEPBackupRestore.class);
        br.getExtension(BackupRestorePluginVersions.class);
    }

}
