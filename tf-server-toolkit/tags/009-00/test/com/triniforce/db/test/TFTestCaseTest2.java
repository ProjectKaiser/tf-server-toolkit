/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import com.triniforce.db.test.TFTestCase.TestLogFactory;
import com.triniforce.utils.ApiAlgs;

public class TFTestCaseTest2 extends TestCase {

	public void testLogFactory() {
		new TFTestCase();
		TestLogFactory lf = new TFTestCase.TestLogFactory();
		Log log = lf.getInstance(getName());
		assertEquals(0, lf.getErrorCount());

		log.error("this is an error 1");
		log.error("this is an error 2");
		log.error("this is an error 3");

		assertEquals(3, lf.getErrorCount());

		log.error("err4", new Exception());

		assertEquals(4, lf.getErrorCount());

		lf.bCountErrors = false;

		log.error("this is an error 5");
		log.error("this is an error 6", new Exception());

		assertEquals(4, lf.getErrorCount());

	}

	public void testLog() throws Exception {
		TFTestCase test = new TFTestCase();
		test.setUp();
		test.test();
		ApiAlgs.getLog(this).error("reason to throw No1");
		boolean bOk = true;
		try {
			test.tearDown();
			bOk = false;
		} catch (AssertionFailedError e) {
		}
		assertTrue(bOk);

		test = new TFTestCase();
		test.setUp();
		test.test();
		test.tearDown();

		test = new TFTestCase();
		test.setUp();
		test.test();
		test.countErrorLogs(false);
		ApiAlgs.getLog(this).error("reason to throw No2");
		test.tearDown();

	}

	public void testLL() throws InterruptedException {
		new TFTestCase();
		// LogFactory.FACTORY_PROPERTY = TestLogFactory.class.getName();
		Thread t = new Thread() {
			@Override
			public void run() {
				ApiAlgs.getLog(this).error("my error");
			}
		};
		t.start();
		t.join();
	}
}
