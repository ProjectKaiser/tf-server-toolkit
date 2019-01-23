/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.srvapi;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.concurrent.SynchronousQueue;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.TaskExecutorsTest;

public class InitFinitTaskWrapperTest extends TFTestCase {
	
	private InitFinitTask mockedCommand;
	private RuntimeException testExceptionInit;
	private RuntimeException testExceptionFinit;
	private RuntimeException testExceptionRun;
	private InitFinitTaskWrapper w;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mockedCommand = mock(InitFinitTask.class);
    	testExceptionInit = new RuntimeException("test exception init");
    	testExceptionFinit = new RuntimeException("test exception finit");
    	testExceptionRun = new RuntimeException("test exception run");
    	w = new InitFinitTaskWrapper(mockedCommand);
	}

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
    
    public void testErrorOnInit() {
    	incExpectedLogErrorCount(1);
    	doThrow(testExceptionInit).when(mockedCommand).init();
    	w.run();
    	assertTrue(w.getTaskState().isBAD());
    	assertEquals(testExceptionInit, w.getTaskState().getAttachedThrowable());
    	assertEquals(w, w.getTaskState().getRelatedObject());
    	
    	// no errors logged on next run
    	w.run();
    	
    	// restore
    	doNothing().when(mockedCommand).init();
    	w.run();
    	assertTrue(w.getTaskState().isOK());
    }
    
    public void testErrorOnInitAndFinit() {
    	incExpectedLogErrorCount(1);
    	doThrow(testExceptionInit).when(mockedCommand).init();
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	assertTrue(w.getTaskState().isBAD());
    	assertEquals(testExceptionFinit, w.getTaskState().getAttachedThrowable());
    	assertEquals(w, w.getTaskState().getRelatedObject());
    	
    	// no errors logged on next run
    	w.run();
    	
    	// restore
    	doNothing().when(mockedCommand).init();
    	doNothing().when(mockedCommand).finit();
    	w.run();
    	assertTrue(w.getTaskState().isOK());
    }
    
    public void testErrorOnRunAndFinit() {
    	incExpectedLogErrorCount(1);
    	doThrow(testExceptionRun).when(mockedCommand).run();
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	assertTrue(w.getTaskState().isBAD());
    	assertEquals(testExceptionFinit, w.getTaskState().getAttachedThrowable());
    	assertEquals(w, w.getTaskState().getRelatedObject());
    	
    	// no errors logged on next run
    	w.run();
    	
    	// restore
    	doNothing().when(mockedCommand).run();
    	doNothing().when(mockedCommand).finit();
    	w.run();
    	assertTrue(w.getTaskState().isOK());
    }
    
    public void testErrorOnRun() {
    	incExpectedLogErrorCount(1);
    	doThrow(testExceptionRun).when(mockedCommand).run();
    	w.run();
    	assertTrue(w.getTaskState().isBAD());
    	assertEquals(testExceptionRun, w.getTaskState().getAttachedThrowable());
    	assertEquals(w, w.getTaskState().getRelatedObject());
    	
    	// no errors logged on next run
    	w.run();
    	
    	// restore
    	doNothing().when(mockedCommand).run();
    	w.run();
    	assertTrue(w.getTaskState().isOK());
    }
    
    public void testErrorOnFinit() {
    	incExpectedLogErrorCount(1);
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	assertTrue(w.getTaskState().isBAD());
    	assertEquals(testExceptionFinit, w.getTaskState().getAttachedThrowable());
    	assertEquals(w, w.getTaskState().getRelatedObject());
    	
    	// no errors logged on next run
    	w.run();
    	
    	// restore
    	doNothing().when(mockedCommand).finit();
    	w.run();
    	assertTrue(w.getTaskState().isOK());
    }
    
    public void testNormalRunState() {
    	w.run();
    	assertTrue(w.getTaskState().isOK());
    }
}
