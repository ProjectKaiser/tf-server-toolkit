/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.services;

import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.utils.ApiStack;

public class QueuedServiceTest extends ServicesTestCase {

    @Override
    public void test() {
        // test init/fini Cycle()
        IDbQueueFactory qf = ApiStack.getApi().getIntfImplementor(
                IDbQueueFactory.class);
        IDbQueue qSM = qf.getDbQueue(SM_ID);

        // clean queue
        while (null != qSM.get(0))
            ;

        // put simpe object
        String testObject1 = "Test object1";
        String testObject2 = "Test object2";
        qSM.put(testObject1);
        qSM.put(testObject2);
        assertNotNull(qSM.peek(0));
        ServiceTest.restartTran(true);
//        if (true)
//            return;

        // Create QueuedService instance and run its initCycle/finitCycle
        // methods
        {
            int oldSize = ApiStack.getThreadApiContainer().getStack().size();
            EP_QueuedService qs = new EP_QueuedService(SM_ID);
            qs.init();
            try {
              

                {// testObject1
                    qs.initCycle();
                    try {
                        assertEquals(testObject1, qs.getItem());
                        qs.commitCycle();
                    } finally {
                        qs.finitCycle();
                    }
                    assertNull(qs.getItem());
                    assertEquals(oldSize, ApiStack.getThreadApiContainer()
                            .getStack().size());
                }
                
                {// testObject2
                    qs.initCycle();
                    try {
                        assertEquals(testObject2, qs.getItem());
                        qs.commitCycle();
                    } finally {
                        qs.finitCycle();
                    }
                    assertNull(qs.getItem());
                    assertEquals(oldSize, ApiStack.getThreadApiContainer()
                            .getStack().size());
                }
            } finally {
                qs.finit();
            }
            assertEquals(oldSize, ApiStack.getThreadApiContainer().getStack()
                    .size());
            assertNull(qSM.peek(0));
        }

    }
}
