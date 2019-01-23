/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.srvapi;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.SynchronousQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.TaskExecutorsTest;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class InitFinitTaskWrapperTest extends TFTestCase {
	
	private InitFinitTask mockedCommand;
	private RuntimeException testExceptionInit;
	private RuntimeException testExceptionFinit;
	private RuntimeException testExceptionRun;
	private InitFinitTaskWrapper w;
	private Log mockedLog;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mockedCommand = mock(InitFinitTask.class);
    	testExceptionInit = new RuntimeException("test exception init");
    	testExceptionFinit = new RuntimeException("test exception finit");
    	testExceptionRun = new RuntimeException("test exception run");
    	w = new InitFinitTaskWrapper(mockedCommand);
    	LogFactory lf = spy(ApiStack.getInterface(LogFactory.class));
    	mockedLog = spy(ApiAlgs.getLog(w));
    	doReturn(mockedLog).when(lf).getInstance(w.getClass());
    	ApiStack.pushInterface(LogFactory.class, lf);
	}
	
	@Override
	public void tearDown() throws Exception {
		ApiStack.popInterface(1);
		super.tearDown();
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
    	incExpectedLogErrorCount(2);
    	doThrow(testExceptionInit).when(mockedCommand).init();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionInit));
    	
    	// no errors logged on next run
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionInit));
    	
    	// restore
    	doNothing().when(mockedCommand).init();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionInit));
    	
    	// error again
    	doThrow(testExceptionInit).when(mockedCommand).init();
    	w.run();
    	verify(mockedLog, times(2)).error(anyString(), eq(testExceptionInit));
    }
    
    public void testErrorOnInitAndFinit() {
    	incExpectedLogErrorCount(2);
    	doThrow(testExceptionInit).when(mockedCommand).init();
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionInit));
    	
    	// no errors logged on next run
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionInit));
    	
    	// restore
    	doNothing().when(mockedCommand).init();
    	doNothing().when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionInit));
    	
    	// error again
    	doThrow(testExceptionInit).when(mockedCommand).init();
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog, times(2)).error(anyString(), eq(testExceptionInit));
    }
    
    public void testErrorOnRunAndFinit() {
    	incExpectedLogErrorCount(2);
    	doThrow(testExceptionRun).when(mockedCommand).run();
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionRun));
    	
    	// no errors logged on next run
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionRun));
    	
    	// restore
    	doNothing().when(mockedCommand).run();
    	doNothing().when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionRun));
    	
    	// error again
    	doThrow(testExceptionRun).when(mockedCommand).run();
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog, times(2)).error(anyString(), eq(testExceptionRun));
    }
    
    public void testErrorOnRun() {
    	incExpectedLogErrorCount(2);
    	doThrow(testExceptionRun).when(mockedCommand).run();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionRun));
    	
    	// no errors logged on next run
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionRun));
    	
    	// restore
    	doNothing().when(mockedCommand).run();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionRun));
    	
    	// error again
    	doThrow(testExceptionRun).when(mockedCommand).run();
    	w.run();
    	verify(mockedLog, times(2)).error(anyString(), eq(testExceptionRun));
    }
    
    public void testErrorOnFinit() {
    	incExpectedLogErrorCount(2);
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionFinit));
    	
    	// no errors logged on next run
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionFinit));
    	
    	// restore
    	doNothing().when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog).error(anyString(), eq(testExceptionFinit));
    	
    	// error again
    	doThrow(testExceptionFinit).when(mockedCommand).finit();
    	w.run();
    	verify(mockedLog, times(2)).error(anyString(), eq(testExceptionFinit));
    }
    
    public void testNoExceptionsOnNormalRun() {
    	w.run();
    }
}
