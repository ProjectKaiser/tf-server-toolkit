package com.triniforce.server.plugins.kernel.ext.api;

import javax.mail.Session;

import com.triniforce.server.plugins.kernel.ext.api.IMailer.EMailerConfigurationError;
import com.triniforce.server.plugins.kernel.ext.api.Mailer.MailData;
import com.triniforce.server.plugins.kernel.services.ServicesTestCase;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;


public class MailerTest extends ServicesTestCase {
	
	//It is necessary to set your own e-mail addresses, user names, passwords, hosts and ports for testing
		
	private static final String TO = "Belaynsky@rambler.ru";
	
	private static final int SMTP_PORT1 = 465;
	private static final int SMTP_PORT2 = 587;
	
	private static final String FROM1 = "MyFio@rambler.ru";
	private static final String SMTP_HOST1 = "smtp.rambler.ru";
	private static final String SMTP_USER1 = "MyFio";
	private static final String SMTP_PASSWORD1 = "****";
	
	private static final String FROM2 = "BelyanskyAA@gmail.com";
	private static final String SMTP_HOST2 = "smtp.gmail.com";
	private static final String SMTP_USER2 = "BelyanskyAA";
	private static final String SMTP_PASSWORD2 = "****";
		
	
	public static class MailerSettingsTest implements IMailerSettings {
		
		private String m_smtpHost;
		private int m_smtpPort;
		private String m_smtpUser;
		private String m_smtpPassword;
		private boolean m_useTLS = false;
		
		public MailerSettingsTest(String smtpHost, int smtpPort, String smtpUser, String smtpPassword, boolean useTLS) {
			m_smtpHost = smtpHost;
			m_smtpPort = smtpPort;
			m_smtpUser = smtpUser;
			m_smtpPassword = smtpPassword;
			m_useTLS = useTLS;
		}

		
		public void loadSettings() {}
		
		public String getSmtpHost() { return m_smtpHost; }
		
		public int getSmtpPort() { return m_smtpPort; }
		
		public String getSmtpUser() { return m_smtpUser; }
		
		public String getSmtpPassword() { return m_smtpPassword; }
		
		public boolean useTLS() { return m_useTLS; }


		public String getDefaultSender() {
			return null;
		}


		@Override
		public Session createSmtpSession() {
			return null;
		}
		
	}

	
	public void testSend() throws EMailerConfigurationError {
	
		Mailer mailer = new Mailer();
		Api api = new Api();
		
		//from rambler by SSL
		api.setIntfImplementor(IMailerSettings.class, new MailerSettingsTest(SMTP_HOST1, SMTP_PORT1, SMTP_USER1, SMTP_PASSWORD1, false));
		ApiStack.pushApi(api);
		try {
			mailer.send(FROM1, TO, "SSL_rambler", "Mailer test");
		} finally{
			ApiStack.popApi();
		}
		
		//from gmail by SSL
		api.setIntfImplementor(IMailerSettings.class, new MailerSettingsTest(SMTP_HOST2, SMTP_PORT1, SMTP_USER2, SMTP_PASSWORD2, false));
		ApiStack.pushApi(api);
		try {
			mailer.send(FROM2, TO, "SSL_gmail", "Mailer test");
		} finally{
			ApiStack.popApi();
		}				
		
		//from gmail by TLS
		api.setIntfImplementor(IMailerSettings.class, new MailerSettingsTest(SMTP_HOST2, SMTP_PORT2, SMTP_USER2, SMTP_PASSWORD2, true));
		ApiStack.pushApi(api);
		try {
			mailer.send(FROM2, TO, "TLS_gmail", "Mailer test");
		} finally{
			ApiStack.popApi();
		}
			
	}
	
	public void testRun() {
		
		Mailer mailer = new Mailer();
		Api api = new Api();
		
		//from rambler by SSL
		api.setIntfImplementor(IMailerSettings.class, new MailerSettingsTest(SMTP_HOST1, SMTP_PORT1, SMTP_USER1, SMTP_PASSWORD1, false));
		ApiStack.pushApi(api);
		try {
			INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
			long mailerId = dbId.createId(IMailer.class.getName());
			IDbQueue mailerQueue = IDbQueueFactory.Helper.getQueue(mailerId);
						
			MailData mailData1 = new MailData(FROM1, TO, "Mailer run test1", null, "test1", null, null, null);
			mailerQueue.put(mailData1);
			
			MailData mailData2 = new MailData(FROM1, TO, "Mailer run test2", null, "test2", null, null, null);
			mailerQueue.put(mailData2);
			
			mailer.run();
			
		} finally{
			ApiStack.popApi();
		}
		
		
		
		
	}
	
	
	
	
	

}
