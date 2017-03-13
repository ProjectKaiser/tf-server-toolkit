/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.triniforce.utils.ApiAlgs;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class TFTestCaseTest2 extends TestCase {

	public void testLogFactory() throws Exception {
		TFTestCase test = new TFTestCase(){
			@Override
			public void test() throws Exception {
				Log log = LogFactory.getFactory().getInstance(TFTestCaseTest2.class);
				log.error("this is an error 1");
			}
		};
		test.setUp();
		assertEquals(0, test.getErrorCount());
		

		test.test();
		test.test();
		test.test();
//		log.error("this is an error 1");
//		log.error("this is an error 2");
//		log.error("this is an error 3");

		assertEquals(3, test.getErrorCount());

		test.test();
//		log.error("err4", new Exception());

		assertEquals(4, test.getErrorCount());

		test.countErrorLogs(false);

		test.test();
		test.test();
//		log.error("this is an error 5");
//		log.error("this is an error 6", new Exception());

		assertEquals(4, test.getErrorCount());

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
}
