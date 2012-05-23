/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ext.br;

import java.io.File;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.plugins.kernel.ep.br.PKEPBackupRestore;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiStack;

public class BackupRestoreDbTest extends BasicServerTestCase {
    @Override
    public void test() throws Exception {

        getServer().enterMode(Mode.Running);
        try {
            IDbQueueFactory qf = ApiStack.getInterface(IDbQueueFactory.class);
            IIdGenerator ig = ApiStack.getInterface(IIdGenerator.class);
            
            File tmpFolder = getTmpFolder(this);
            File archive = new File(tmpFolder, "dbarchive.zip");

            PKEPBackupRestore br = (PKEPBackupRestore) getServer()
                    .getExtensionPoint(PKEPBackupRestore.class);
            br.getExtension(BackupRestoreDb.class);
            
            //put a record into queue
            
            Long qid = ig.getKey();
            trace(qid);
            IDbQueue dbq = qf.getDbQueue(qid);
            dbq.put(new String("Hello"));
            ISrvSmartTranFactory.Helper.commitAndStartTran();
            assertNotNull(dbq.peek(0));

            //backup
            br.backup(archive);

            //drop record from queue
            dbq.get(0);
            ISrvSmartTranFactory.Helper.commitAndStartTran();
            assertNull(dbq.peek(0));
            br.restore(archive);
            
            //record should be there
            ISrvSmartTranFactory.Helper.commitAndStartTran();
            assertNotNull(dbq.peek(0));
        } finally {
            getServer().leaveMode();
        }
    }

}
