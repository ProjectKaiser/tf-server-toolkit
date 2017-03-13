/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
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
	
	public static void main(String[] args) {
		GreenMail svc = new GreenMail();
		svc.start();
		try{
			System.out.println("GreenMail SMTP server statred");
			System.out.println("SMTP port: " + ServerSetupTest.SMTP.getPort());
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			String s;
			do{
				System.out.println(">");
				s = r.readLine();
				if(s.startsWith("s")){
					System.out.println("Messages: ");
					for(MimeMessage mm : svc.getReceivedMessages()){
						System.out.println(GreenMailUtil.getBody(mm));
					}
				}
			}while(!s.toLowerCase().startsWith("q"));
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			svc.stop();
		}
		
		
		
	}
}
