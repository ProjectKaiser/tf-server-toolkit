/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.intf;

import com.triniforce.db.test.TFTestCase;

public class QSyncQueueInfoTest extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        QSyncQueueInfo qi = new QSyncQueueInfo();
        qi.setLastFailure(1);
        qi.setLastSuccess(2);
        assertEquals(2, qi.getLastAttempt());
        qi.setLastFailure(3);
        assertEquals(3, qi.getLastAttempt());
        trace(qi);
    }

}
