/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.ext.api;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIs;
import com.triniforce.server.plugins.kernel.ext.api.Mailer.MailData;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class MailerSrvTest extends BasicServerTestCase {
	
	static int TEST_SM_ID = 10001;
	
	static class TestMS implements IMailerSettings{
		public void loadSettings() {
		}

		public String getSmtpHost() {
			return "localhost";
		}

		public int getSmtpPort() {
			return ServerSetupTest.SMTP.getPort();
		}

		public String getSmtpUser() {
			return "alex@kukuruku.com";
		}

		public String getSmtpPassword() {
			return "PWD_TRR";
		}

		public boolean useTLS() {
			return false;
		}
		
	}
	
	public static class TestMailer extends Mailer{
		public TestMailer() {
			super();
			initialDelay = 0L;
		}
		
		@Override
		public void send(String from, String to, String subject, String body) {
			super.send(from, to, subject, body);
			ApiAlgs.getLog(this).trace("MAILER: "+from + ", subj:" + subject);
		}
		
	}
	
	static class MailerSrvPlugin extends PKPlugin{

		@Override
		public void doRegistration() {
	        putExtension(PKEPAPIs.class, TestMailer.class);
		}

		@Override
		public void doExtensionPointsRegistration() {
		}
		
	}

	GreenMail greenMail = new GreenMail(); // uses test ports by default
	
	@Override
	protected void setUp() throws Exception {
		greenMail.start();
		addPlugin(new MailerSrvPlugin());
		super.setUp();

	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		greenMail.stop();
	}
	
	@Override
	protected void setCoreApiInteraces(Api api) {
		super.setCoreApiInteraces(api);
		api.setIntfImplementor(IMailerSettings.class, new TestMS());
	}
	
	@Override
	public void test() throws Exception {
		{
			sendMailAttach("plain", null);
			
			getServer().startPeriodicalTasks();
			Thread.sleep(500L);
			getServer().stopPeriodicalTasks();
			
			MimeMessage msg1 = greenMail.getReceivedMessages()[0];
			
			assertEquals("body", GreenMailUtil.getBody(msg1));
			Address[] from = msg1.getFrom();
			assertEquals("test1@tur.com", from[0].toString());
			
			assertFalse(GreenMailUtil.hasNonTextAttachments(msg1));
			assertEquals("text/plain; charset=UTF-8", msg1.getContentType());
		}
		

//		greenMail.getManagers().getImapHostManager().getInbox(user)
		{
			sendMailAttach("html", "TEST_ATTACH".getBytes());
			getServer().startPeriodicalTasks();
			Thread.sleep(500L);
			getServer().stopPeriodicalTasks();
			
			MimeMessage msg1 = greenMail.getReceivedMessages()[1];
			
			assertTrue(GreenMailUtil.hasNonTextAttachments(msg1));
			
			MimeMultipart mm = (MimeMultipart) msg1.getContent();
			BodyPart bin1 = mm.getBodyPart(1);
			assertEquals("application/pdf; name=invoice.pdf", bin1.getContentType());
			byte[] buf = new byte[100];
			int nr = bin1.getDataHandler().getInputStream().read(buf);
			assertEquals("TEST_ATTACH", new String(buf, 0, nr));
			
			BodyPart bin2 = mm.getBodyPart(0);
			assertEquals("text/html; charset=UTF-8", bin2.getContentType());
			
		}


	}

	private void sendMailAttach(String bodyType, byte[] attach){
		getServer().enterMode(Mode.Running);
		try{
			MailData data = new Mailer.MailData("test1@tur.com", "test2@tur.com", "TEST_MSG_SUBJ", 
					bodyType, "body", "application/pdf", "invoice.pdf", attach);
			INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
			long mailerId = dbId.createId(IMailer.class.getName());
			
			IDbQueue mailerQueue = IDbQueueFactory.Helper.getQueue(mailerId);
			mailerQueue.put(data);
			ApiAlgs.getLog(this).trace("Mail sent to queue: " + mailerId);
			SrvApiAlgs2.getIServerTran().commit();
		}finally{
			getServer().leaveMode();
		}
	}
}
