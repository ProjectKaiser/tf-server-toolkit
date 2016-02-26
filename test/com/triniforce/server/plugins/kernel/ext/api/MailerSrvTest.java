/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.ext.api;

import java.util.Arrays;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Session;
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

	private static String SMTP_USER = "alex@kukuruku.com";
	
	static String DEF_SENDER = null;
	
	int SMTP_PORT = ServerSetupTest.SMTP.getPort();
	String SMTP_HOST = "localhost";

	class TestMS implements IMailerSettings{
		
		public void loadSettings() {
		}

		public String getSmtpHost() {
			return SMTP_HOST;
		}

		public int getSmtpPort() {
			return SMTP_PORT;
		}

		public String getSmtpUser() {
			return SMTP_USER;
		}

		public String getSmtpPassword() {
			return "PWD_TRR";
		}

		public boolean useTLS() {
			return false;
		}

		public String getDefaultSender() {
			return DEF_SENDER;
		}
		
	}
	
	static RuntimeException SENDING_ERROR = null;
	
	public static class TestMailer extends Mailer{
		public TestMailer() {
			super();
			initialDelay = 0L;
		}
		
		@Override
		public boolean send(String from, String to, String subject, String body) throws EMailerConfigurationError {
			if(null != SENDING_ERROR)
				throw SENDING_ERROR;
			boolean res = super.send(from, to, subject, body);
			ApiAlgs.getLog(this).trace("MAILER: "+from + ", subj:" + subject);
			return res;
		}
		
		@Override
		public synchronized boolean send(String from, String to,
				String subject, String bodyType, String body,
				String attachFile, String attachType, byte[] attachment)
				throws EMailerConfigurationError {
			if(null != SENDING_ERROR)
				throw SENDING_ERROR;
			return super.send(from, to, subject, bodyType, body, attachFile, attachType,
					attachment);
		}

		public Session getActiveSession() {
			return m_session;
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
		getServer().enterMode(Mode.Running);
		try{
			IDbQueueFactory.Helper.cleanQueue(ApiStack.getInterface(INamedDbId.class).createId(IMailer.class.getName()));
		}finally{
			getServer().leaveMode();
		}
		SENDING_ERROR = null;

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
			
			waitForMailer();
			
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
			waitForMailer();
			
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
		{
			DEF_SENDER = "default@mail.com";
			sendMailAttach(null, "plain", "5675".getBytes());
			waitForMailer();
			MimeMessage msg1 = greenMail.getReceivedMessages()[2];
			assertEquals("default@mail.com", msg1.getFrom()[0].toString());
			
		}
	}

    private void waitForMailer() throws InterruptedException {
		getServer().enterMode(Mode.Running);
		try{
			Mailer m = getMailer();
			m.init();
			try{
				m.run();
				m.commit();
			}finally{
				m.finit();
			}
		}finally{
			getServer().leaveMode();
		}
//		getServer().startPeriodicalTasks();
//		Thread.sleep(500L);
//		getServer().stopPeriodicalTasks();
	}

	private void sendMailAttach(String from, String bodyType, byte[] attach){
		getServer().enterMode(Mode.Running);
		try{
			MailData data = new Mailer.MailData(from, "test2@tur.com", "TEST_MSG_SUBJ", 
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
	
	private void sendMailAttach(String bodyType, byte[] attach){
		sendMailAttach("test1@tur.com", bodyType, attach);
	}
	
	public void testSession() throws InterruptedException{
		assertTrue(Arrays.asList().equals(Arrays.asList()));
		
		TestMailer mailer = (TestMailer) getMailer();
		assertNotNull(mailer);
		Session session;
		
		sendMail();
		assertNotNull(session = mailer.getActiveSession());

		sendMail();		
		assertSame(session, mailer.getActiveSession());
		
		SMTP_USER = "testSessionUser@test.com";		
		sendMail();
		Session s2;
		assertNotSame(session, s2 = mailer.getActiveSession());

		greenMail.stop();
		// Should be failed and secondary send with recreated session
		sendMailAttach("plain", "ss".getBytes());
		waitForMailer();
		
		assertNotSame(s2, mailer.getActiveSession());
		
		incExpectedLogErrorCount(1);
		
	}

	private void sendMail() throws InterruptedException {
		int before = greenMail.getReceivedMessages().length;
		sendMailAttach("plain", "ss".getBytes());
		waitForMailer();
		assertEquals(before+1, greenMail.getReceivedMessages().length);
	}

	private Mailer getMailer() {
		return getServer().getExtension(PKEPAPIs.class, TestMailer.class).getInstance();
	}
	
	public void testSendMailErrorConfiguration() throws InterruptedException{
		
		SMTP_PORT = 52636;
		
		sendMailAttach("plain", "ss".getBytes());
		waitForMailer();
		
		incExpectedLogErrorCount(1);

		
		getServer().enterMode(Mode.Running);
		try{
			INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
			long mailerId = dbId.createId(IMailer.class.getName());
			
			IDbQueue mailerQueue = IDbQueueFactory.Helper.getQueue(mailerId);
			MailData res = (MailData) mailerQueue.get(10L);
			assertNotNull(res);
		}finally{
			getServer().leaveMode();
		}
		
		getMailer().m_nextExecTime = 0L; // No Error timeouts
		
		//2 emails in queue
		sendMailAttach("plain", "ss_2".getBytes());
		waitForMailer();
		incExpectedLogErrorCount(1);

		SMTP_HOST = null;		
		waitForMailer();
		
		getServer().enterMode(Mode.Running);
		try{
			INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
			long mailerId = dbId.createId(IMailer.class.getName());
			
			IDbQueue mailerQueue = IDbQueueFactory.Helper.getQueue(mailerId);
			assertNotNull(mailerQueue.get(0L));
			assertNotNull(mailerQueue.get(0L));
			
			
			
		}finally{
			getServer().leaveMode();
		}
	}
	
	public void testNextExecTime() throws InterruptedException{
		SMTP_PORT = 52636;
		getBasicSrvApiEmu().setTimeSeq(50000, new long[]{10});
		
		sendMailAttach("plain", "ss".getBytes());
		waitForMailer();
		
		incExpectedLogErrorCount(1);

		
		getServer().enterMode(Mode.Running);
		try{
			assertTrue("" + getMailer().m_nextExecTime, getMailer().m_nextExecTime > 50000); 
		}finally{
			getServer().leaveMode();
		}
		
		waitForMailer(); // No error because Error timeout is not reached

	}
	
	public void testIsMailConfigured(){
		getServer().enterMode(Mode.Running);
		try{
			Mailer m = getMailer();
			assertTrue(m.isMailerConfigured());
			
			SMTP_HOST = null;
			assertFalse(m.isMailerConfigured());
		}finally{
			getServer().leaveMode();
		}
		
	}
	
	public void testSendMailError() throws InterruptedException{
		sendMailAttach("plain", "ss".getBytes());
		SENDING_ERROR = new RuntimeException();
		waitForMailer();
		
		getServer().enterMode(Mode.Running);
		try{
			INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
			long mailerId = dbId.createId(IMailer.class.getName());
			
			IDbQueue mailerQueue = IDbQueueFactory.Helper.getQueue(mailerId);
			MailData res = (MailData) mailerQueue.get(10L);
			assertNotNull(res);
		}finally{
			getServer().leaveMode();
		}
		
	}
}
