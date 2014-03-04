package com.triniforce.server.plugins.kernel.ext.api;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIPeriodicalTask;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.INamedDbId;
//import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiAlgs.RethrownException;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.InSeparateThreadExecutor;
import com.triniforce.utils.InSeparateThreadExecutor.IRunnable;
import com.triniforce.utils.Utils;

public class Mailer extends PKEPAPIPeriodicalTask implements IMailer, IPKEPAPI {
	
	private static final String emailCharset = Charset.forName("UTF-8").name();
	
	public Mailer() {
		super();
        delay = 5000;
        initialDelay = 5000;
	}
	
	
	public Class getImplementedInterface() {
		return IMailer.class;
	}

	@Override
	public void run() {
		
		INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
		long mailerId = dbId.createId(IMailer.class.getName());
		
		IDbQueue mailerQueue = IDbQueueFactory.Helper.getQueue(mailerId);
		
		Object obj = mailerQueue.get(0l);
		while (obj != null) {
			
			MailData mailData = (MailData)obj;
			
			this.send(mailData.getFrom(), mailData.getTo(), mailData.getSubject(), mailData.getBody());
			
			obj = mailerQueue.get(0l);
		}
	}
	
	public static class MailData implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String m_from; 
		private String m_to;
		private String m_subject;
		private String m_body;
		
		public MailData(String from, String to, String subject, String body) {
			m_from = from;
			m_to = to;
			m_subject = subject;
			m_body = body;
		}
		
		public String getFrom() {
			return m_from;
		}
		public String getTo() {
			return m_to;
		}
		public String getSubject() {
			return m_subject;
		}
		public String getBody() {
			return m_body;
		}
		public void setFrom(String from) {
			this.m_from = from;
		}
		public void setTo(String to) {
			this.m_to = to;
		}
		public void setSubject(String subject) {
			this.m_subject = subject;
		}
		public void setBody(String body) {
			this.m_body = body;
		}
	}
		
	
	public void send(String from, String to, String subject, String body) {
		
		IMailerSettings mailerSettings  = ApiStack.queryInterface(IMailerSettings.class);
		
		if (mailerSettings == null) {
			ApiAlgs.getLog(this).warn("Mailer settings is null, message is skipped: " + body); //$NON-NLS-1$
			return;
		}
		
		mailerSettings.loadSettings();
				
        String smtpHost = mailerSettings.getSmtpHost();
        if (Utils.isEmptyString(smtpHost)) {
            ApiAlgs.getLog(this).warn("SMTP server is not configured, message is skipped: " + body); //$NON-NLS-1$
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);//$NON-NLS-1$
        props.put("mail.smtp.port", mailerSettings.getSmtpPort());        
        Integer innerTimeout = 4*60*1000;
        Integer outerTimeout = innerTimeout  +  innerTimeout/4;
        
        String strTimeout = innerTimeout.toString();
        props.put("mail.smtp.connectiontimeout",strTimeout);
        props.put("mail.smtp.timeout", strTimeout);
        props.put("mail.smtps.connectiontimeout", strTimeout);
        props.put("mail.smtps.timeout", strTimeout);
        
        
        if(mailerSettings.useTLS() == true){
            props.put("mail.smtp.starttls.enable", "true");
        } else {
        	props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        
        //IThrdWatcherRegistrator twr = ApiStack
        //        .getInterface(IThrdWatcherRegistrator.class);

        //twr.registerLongTermOp(Thread.currentThread());
        
        try{
        	Session session = null;
            final String smtpPwd = mailerSettings.getSmtpPassword();
            final String smtpUsr = mailerSettings.getSmtpUser();
            if (Utils.isEmptyString(smtpUsr)) {
                session = Session.getInstance(props, null);
            }else{
                props.put("mail.smtp.auth", "true");
                javax.mail.Authenticator a = new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsr, smtpPwd);
                    }
                }; 
                session = Session.getInstance(props, a);            
            }
            
            MimeMessage msg = null;
            try {
            	
            	msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(from));
                msg.setRecipient(RecipientType.TO, new InternetAddress(to));
                msg.setSubject(subject, emailCharset);
            	msg.setText(body, emailCharset);
                
            } catch (Throwable t) {
            	ApiAlgs.getLog(this).error("Error preparing mail, mail is skipped",t);//$NON-NLS-1$
                return;
            }
            ApiAlgs.getLog(this).trace("Sending email:"+ body);//$NON-NLS-1$
            final MimeMessage finalMsg = msg; 
            try{
            	IRunnable r = new InSeparateThreadExecutor.IRunnable(){
                    public void run() throws Exception{
                        Transport.send(finalMsg);
                    }
                };
                InSeparateThreadExecutor ex = new InSeparateThreadExecutor();
                ex.execute("Send mail", r, outerTimeout);
                ApiAlgs.getLog(this).trace("Mail sent");

            } catch(RethrownException e){
                ApiAlgs.getLog(this).error("Error sending mail, mail is skipped", e.getCause());
            }
        }finally{
        	//twr.unregisterLongTermOp(Thread.currentThread());
        }
	
	}
	
	
	

}
