/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package gmp;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.utils.ICheckInterrupted;

public class InvPeriodicalTest extends BasicServerTestCase{
    @Override
    public void test() throws Exception {
        m_server.startPeriodicalTasks();
        try {
            ICheckInterrupted.Helper.sleep(20000);
        } finally {
            m_server.stopPeriodicalTasks();
        }

    }

}
