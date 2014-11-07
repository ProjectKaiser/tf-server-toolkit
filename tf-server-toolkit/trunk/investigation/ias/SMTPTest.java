/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.triniforce.db.test.TFTestCase;

public class SMTPTest extends TFTestCase {

	@Override
	public void test() throws Exception {
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
}
