package com.triniforce.server.plugins.kernel.ext.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIPeriodicalTask;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
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
			
			if(null == mailData.getAttachment())
				this.send(mailData.getFrom(), mailData.getTo(), mailData.getSubject(), mailData.getBody());
			else
				this.send(mailData.getFrom(), mailData.getTo(), mailData.getSubject(), 
						mailData.getBodyType(), mailData.getBody(),
						mailData.getAttachFileName(), mailData.getAttachType(), mailData.getAttachment());
			
			obj = mailerQueue.get(0l);
		}
	}
	
	public static class MailData implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private String m_from; 
		private String m_to;
		private String m_subject;
		private String m_body;
		private byte[] m_attachment;
		private String m_attachFileName;
		private String m_attachType;
		private String m_bodyType;
		
		public MailData(String from, String to, String subject, String bodyType, String body, 
				String attachType, String attachFName, byte[] attach) {
			m_from = from;
			m_to = to;
			m_subject = subject;
			m_body = body;
			setBodyType(bodyType);
			setAttachType(attachType);
			setAttachment(attach);
			setAttachFileName(attachFName);
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

		public byte[] getAttachment() {
			return m_attachment;
		}

		public void setAttachment(byte[] attachment) {
			m_attachment = attachment;
		}

		public String getAttachFileName() {
			return m_attachFileName;
		}

		public void setAttachFileName(String attachFilName) {
			m_attachFileName = attachFilName;
		}

		public String getAttachType() {
			return m_attachType;
		}

		public void setAttachType(String attachType) {
			m_attachType = attachType;
		}

		public String getBodyType() {
			return m_bodyType;
		}

		public void setBodyType(String bodyType) {
			m_bodyType = bodyType;
		}
	}

	private MimeBodyPart settAttachment(final String filename, 
			final String contentType, final byte[] attachment, String description) throws MessagingException {

		MimeBodyPart binaryPart = new MimeBodyPart();
        DataSource ds = new DataSource() {
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(attachment);
            }

            public OutputStream getOutputStream() throws IOException {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byteStream.write(attachment);
                return byteStream;
            }

            public String getContentType() {
                return contentType;
            }

            public String getName() {
                return filename;
            }
        };
        binaryPart.setDataHandler(new DataHandler(ds));
        binaryPart.setFileName(filename);
        binaryPart.setDescription(description);

        return binaryPart;		
	}


	public void send(String from, String to, String subject, String body) {
		send(from, to, subject, null, body, null, null, null);
		
	}


	public void send(String from, String to, String subject, 
			String bodyType, String body,
			String attachFile, String attachType, byte[] attachment) {
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
        
        IThrdWatcherRegistrator twr = ApiStack
                .getInterface(IThrdWatcherRegistrator.class);

        twr.registerLongTermOp(Thread.currentThread());
        
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
                
                if(null == attachment){
                	msg.setText(body, emailCharset);
                }
                else{
                	MimeMultipart multiPart = new MimeMultipart();

                    MimeBodyPart textPart = new MimeBodyPart();
                    multiPart.addBodyPart(textPart);
                    textPart.setText(body, emailCharset, bodyType);

                	MimeBodyPart binary = settAttachment(attachFile, "application/pdf", attachment, "");
                    multiPart.addBodyPart(binary);
                    
                    msg.setContent(multiPart);
                }
                
            } catch (Throwable t) {
            	ApiAlgs.getLog(this).error("Error preparing mail, mail is skipped",t);//$NON-NLS-1$
                return;
            }
            ApiAlgs.getLog(this).info("Sending email:"+ body);//$NON-NLS-1$
            final MimeMessage finalMsg = msg; 
            try{
            	IRunnable r = new InSeparateThreadExecutor.IRunnable(){
                    public void run() throws Exception{
                        Transport.send(finalMsg);
                    }
                };
                InSeparateThreadExecutor ex = new InSeparateThreadExecutor();
                ex.execute("Send mail", r, outerTimeout);
                ApiAlgs.getLog(this).info("Mail sent");

            } catch(RethrownException e){
                ApiAlgs.getLog(this).error("Error sending mail, mail is skipped", e.getCause());
            }
        }finally{
        	twr.unregisterLongTermOp(Thread.currentThread());
        }
		
	}
	
	
	

}
