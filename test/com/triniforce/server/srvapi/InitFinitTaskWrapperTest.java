/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.srvapi;

import java.util.concurrent.SynchronousQueue;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.TaskExecutorsTest;

public class InitFinitTaskWrapperTest extends TFTestCase {

    @Override
    public void test() throws Exception {
        SynchronousQueue q = new SynchronousQueue();
        InitFinitTask t1 = new TaskExecutorsTest.NamedTestTask("t1", true, q);
        InitFinitTask t2 = new TaskExecutorsTest.NamedTestTask("t2", true, q);
        InitFinitTaskWrapper w1 = new InitFinitTaskWrapper(t1);
        InitFinitTaskWrapper w2 = new InitFinitTaskWrapper(t1);
        InitFinitTaskWrapper w3 = new InitFinitTaskWrapper(t2);
        assertEquals(w1, w2);
        assertFalse(w1.equals(w3));
        assertTrue(w2.equals(w1));
    }

    public void test_isSeriousException() {
        assertTrue(InitFinitTaskWrapper
                .isSeriousException(new RuntimeException("")));
        assertFalse(InitFinitTaskWrapper
                .isSeriousException(new RuntimeException(
                        "Unable to complete network request to host")));
    }

}
