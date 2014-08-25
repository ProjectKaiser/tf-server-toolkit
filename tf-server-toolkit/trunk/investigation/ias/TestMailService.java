/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.triniforce.db.test.BasicServerTestCase;

public class TestMailService extends BasicServerTestCase {

	GreenMail greenMail = new GreenMail(); // uses test ports by default
	
	@Override
	protected void setUp() throws Exception {
		greenMail.start();
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		greenMail.stop();
	}
	
	@Override
	public void test() throws Exception {
		
		send("to@localhost.com", "from@localhost.com", "subject", "body");

		assertEquals("body",
				GreenMailUtil.getBody(greenMail.getReceivedMessages()[0]));
	}

	private void send(String from, String to, String subj,
			String body) {
		
	}
}
