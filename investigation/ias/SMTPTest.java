/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.triniforce.db.test.TFTestCase;

public class SMTPTest extends TFTestCase {

	public void ntest() throws Exception {
		{
	        Properties props = new Properties();
	        props.setProperty("mail.smtp.host", "sigma-soft.spb.ru");
	        // props.setProperty("mail.smtp.auth", "true"); // not necessary for my server, I'm not sure if you'll need it
	        Session session = Session.getInstance(props, null);
	        Transport transport = session.getTransport("smtp");
	        transport.connect("ias", null);

	        Message message = new MimeMessage(session);
	        message.setSubject("Test");
	        message.setText("Hello :)");
	        message.setFrom(new InternetAddress("ias@sigma-soft.spb.ru"));
	        message.setRecipient(Message.RecipientType.TO, new InternetAddress("bckbox@inbox.ru"));
	        transport.sendMessage(message, message.getAllRecipients());
	    }
	}
	
	public void test2() throws AddressException, MessagingException {

		Session session;
		Properties props;
		{
			props = new Properties();
			String strTimeout = Integer.valueOf(4 * 60 * 1000).toString();
			props.put("mail.smtp.connectiontimeout", strTimeout);
			props.put("mail.smtp.timeout", strTimeout);
			props.put("mail.smtps.connectiontimeout", strTimeout);
			props.put("mail.smtps.timeout", strTimeout);
			props.put("mail.smtp.host", "sigma-soft.spb.ru");
			props.put("mail.smtp.port", 25);

			// if(mailerSettings.useTLS() == true){
			// props.put("mail.smtp.starttls.enable", "true");
			// } else {
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			// }
			// String smtpUsr = mailerSettings.getSmtpUser();
			// if (!Utils.isEmptyString(smtpUsr)) {
//			props.put("mail.smtp.auth", "true");
			// }
			// return props;
		}

		javax.mail.Authenticator authenticator = null;

//		authenticator = new javax.mail.Authenticator() {
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication("ias", null);
//			}
//		};
		session = Session.getInstance(props, authenticator);

		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress("ias@sigma-soft.spb.ru"));
		msg.setRecipient(RecipientType.TO, new InternetAddress(
				"bckboxUNKNOWN@inbox.1231231ru"));
		msg.setSubject("test2", "utf-8");
		msg.setText("body2", "utf-8");
		
		Transport.send(msg);

	}
}
