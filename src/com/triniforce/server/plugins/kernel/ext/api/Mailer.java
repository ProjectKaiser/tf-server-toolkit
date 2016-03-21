package com.triniforce.server.plugins.kernel.ext.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
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
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import com.triniforce.server.plugins.kernel.ep.api.IPKEPAPI;
import com.triniforce.server.plugins.kernel.ep.api.PKEPAPIPeriodicalTask;
import com.triniforce.server.srvapi.IDbQueue;
import com.triniforce.server.srvapi.IDbQueueFactory;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.IThrdWatcherRegistrator;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiAlgs.RethrownException;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IRunnable;
import com.triniforce.utils.ITime;
import com.triniforce.utils.InSeparateThreadExecutor;
import com.triniforce.utils.TFUtils;
import com.triniforce.utils.Utils;

/**
 * @author ias
 *
 */
/**
 * @author ias
 *
 */
public class Mailer extends PKEPAPIPeriodicalTask implements IMailer, IPKEPAPI {
	
	private static final String emailCharset = Charset.forName("UTF-8").name();
	
    static final int innerTimeout = 4*60*1000;
    static final int outerTimeout = innerTimeout  +  innerTimeout/4;
    
    static final int ERROR_SEND_TIMEOUT = 1 * 60 * 1000; // 1 minute after error
    
    static final Properties INI_SESSION_PROPS = new Properties();
    static{
        String strTimeout = Integer.valueOf(innerTimeout).toString();
        INI_SESSION_PROPS.put("mail.smtp.connectiontimeout",strTimeout);
        INI_SESSION_PROPS.put("mail.smtp.timeout", strTimeout);
        INI_SESSION_PROPS.put("mail.smtps.connectiontimeout", strTimeout);
        INI_SESSION_PROPS.put("mail.smtps.timeout", strTimeout);
    }

	
	protected List<Object> m_sessionKey;
	protected Session m_session = null;
	
	/**
	 * Next run will be excecuted if this time is less then current
	 */
	protected long m_nextExecTime=0L; 
	
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
		if(m_nextExecTime > ApiStack.getInterface(ITime.class).currentTimeMillis()){
			return ;
		}
		
		INamedDbId dbId = ApiStack.getInterface(INamedDbId.class);
		long mailerId = dbId.createId(IMailer.class.getName());
		
		IDbQueue mailerQueue = IDbQueueFactory.Helper.getQueue(mailerId);
		

		Object obj;
		while ((obj = mailerQueue.get(0l)) != null) {
			
			MailData mailData = (MailData)obj;
			boolean bMailSent;
			
			try{
				if(null == mailData.getAttachment())
					bMailSent = this.send(mailData.getFrom(), mailData.getTo(), mailData.getSubject(), mailData.getBody());
				else
					bMailSent = this.send(mailData.getFrom(), mailData.getTo(), mailData.getSubject(), 
							mailData.getBodyType(), mailData.getBody(),
							mailData.getAttachFileName(), mailData.getAttachType(), mailData.getAttachment());
			}catch(IMailer.EMailerConfigurationError e){
				ApiAlgs.getLog(this).warn("Mailer is not configured");
				bMailSent = false;					
			}
			catch (Exception e) {
				ApiAlgs.getLog(this).warn("Mail is not sent", e);
				bMailSent = false;					
			}
			
			if(!bMailSent){
				mailerQueue.put(mailData);
				m_nextExecTime = ApiStack.getInterface(ITime.class).currentTimeMillis() + ERROR_SEND_TIMEOUT;
				break;
			}
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
		
		public MailData(String from, String to, String subject, String body) {
			this(from, to, subject, null, body, null, null, null);
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


	public boolean send(String from, String to, String subject, String body) throws EMailerConfigurationError {
		return send(from, to, subject, null, body, null, null, null);
	}


	public synchronized boolean send(String from, String to, final String subject, 
			final String bodyType, final String body,
			String attachFile, String attachType, byte[] attachment) throws EMailerConfigurationError {
		final IMailerSettings mailerSettings  = ApiStack.queryInterface(IMailerSettings.class);
		
		if(!isMailerConfigured()){
			ApiAlgs.getLog(this).warn("Message is skipped: " + body); //$NON-NLS-1$
			throw new EMailerConfigurationError();
		}
		
        IThrdWatcherRegistrator twr = ApiStack
                .getInterface(IThrdWatcherRegistrator.class);

        twr.registerLongTermOp(Thread.currentThread());
        
        try{
        	
        	List<Object> oldSessionKey = m_sessionKey; 
        	Session session = getActiveSession(mailerSettings);
        	final boolean bSessionRecreated = !TFUtils.equals(oldSessionKey, m_sessionKey);
            
        	MimeMessage msg;
        	final InternetAddress fromAddr;
        	final InternetAddress[] toAddr;
        	final MimeBodyPart attach;
            try {
            	if(null == from){
            		from = mailerSettings.getDefaultSender();
            	}
            	fromAddr = new InternetAddress(from);
            	toAddr = InternetAddress.parse(to);
           		attach = (null != attachment) ? settAttachment(attachFile, attachType, attachment, "") : null;
            	msg = createMessage(session, fromAddr, toAddr, subject, bodyType, body, attach);
            } catch (Throwable t) {
            	ApiAlgs.getLog(this).error("Error preparing mail, mail is skipped",t);//$NON-NLS-1$
                return false;
            }
            ApiAlgs.getLog(this).info("Sending email:"+ body);//$NON-NLS-1$
            final MimeMessage finalMsg = msg; 
            try{
            	IRunnable  r = new IRunnable(){
                    public void run() throws Exception{
                    	try{
                    		Transport.send(finalMsg);
                    	}catch(Exception e){
                    		if(!bSessionRecreated){
	                    		ApiAlgs.getLog(this).warn("Session invalidated", e);
	                    		Session session = reopenSession(mailerSettings);
	                    		MimeMessage msg2 = createMessage(session, fromAddr, toAddr, subject, bodyType, body, attach);
	                    		Transport.send(msg2);
                    		}
                    		else{
                    			throw e;
                    		}
                    	}
                    }

					
                };
                InSeparateThreadExecutor ex = new InSeparateThreadExecutor();
                ex.execute("Send mail", r, outerTimeout);
                ApiAlgs.getLog(this).info("Mail sent");
                return true;
            } catch(RethrownException e){
                ApiAlgs.getLog(this).error("Error sending mail, mail is skipped", e.getCause());
                return false;
            }
        }finally{
        	twr.unregisterLongTermOp(Thread.currentThread());
        }
		
	}


	private MimeMessage createMessage(Session session, InternetAddress from, InternetAddress[] to, 
			String subject, String bodyType, String body, MimeBodyPart attachment) throws MessagingException{
		MimeMessage msg = new MimeMessage(session);
        msg.setFrom(from);
        msg.addRecipients(RecipientType.TO, to);
        msg.setSubject(subject, emailCharset);
        
        if(null == attachment){
        	msg.setText(body, emailCharset);
        }
        else{
        	MimeMultipart multiPart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            multiPart.addBodyPart(textPart);
            textPart.setText(body, emailCharset, bodyType);

        	multiPart.addBodyPart(attachment);
            
            msg.setContent(multiPart);
        }
        return msg;
	}

	private Session reopenSession(final IMailerSettings mailerSettings) {
		Properties props = createSessionProperties(mailerSettings);
		final String smtpUsr = mailerSettings.getSmtpUser();
        javax.mail.Authenticator authenticator = null;
	    if (!Utils.isEmptyString(smtpUsr)) {
            authenticator = new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsr, 
                    		"".equals(mailerSettings.getSmtpPassword()) ? null : mailerSettings.getSmtpPassword());
                }
            };
        }
		m_session = Session.getInstance(props, authenticator);
		return m_session;
	}
	
	private Session getActiveSession(IMailerSettings mailerSettings) {
		Session result;
        Properties props = createSessionProperties(mailerSettings); 
        
        final String smtpPwd = "".equals(mailerSettings.getSmtpPassword()) ? null : mailerSettings.getSmtpPassword();
        final String smtpUsr = mailerSettings.getSmtpUser();
        
        javax.mail.Authenticator authenticator = null;
        
        List<Object> key = createSessionKey(props, smtpUsr, smtpPwd);
    	if(null == m_session || !m_sessionKey.equals(key)){
            if (!Utils.isEmptyString(smtpUsr)) {
                authenticator = new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsr, smtpPwd);
                    }
                };
            }
            
            result = Session.getInstance(props, authenticator); 
            m_sessionKey = key;
            m_session = result;
    	}
    	else
    		result = m_session;
    	
    	return result;
    		
	}


	private Properties createSessionProperties(IMailerSettings mailerSettings) {
		Properties props = (Properties) INI_SESSION_PROPS.clone();
        props.put("mail.smtp.host", mailerSettings.getSmtpHost());
        props.put("mail.smtp.port", mailerSettings.getSmtpPort());        
        
        if(mailerSettings.useTLS() == true){
            props.put("mail.smtp.starttls.enable", "true");
        } else {
        	props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        String smtpUsr = mailerSettings.getSmtpUser();
        if (!Utils.isEmptyString(smtpUsr)) {
            props.put("mail.smtp.auth", "true");
        }
		return props;
	}


	private List<Object> createSessionKey(Properties props, String smtpUser, String smtpPwd) {
		return Arrays.asList((Object)props, smtpUser, smtpPwd);
	}


	public boolean isMailerConfigured() {
		final IMailerSettings mailerSettings  = ApiStack.queryInterface(IMailerSettings.class);
		
		if (mailerSettings == null) {
			ApiAlgs.getLog(this).warn("Mailer settings is null"); //$NON-NLS-1$
			return false;
		}
		
		mailerSettings.loadSettings();
		
        String smtpHost = mailerSettings.getSmtpHost();
        if (Utils.isEmptyString(smtpHost)) {
            ApiAlgs.getLog(this).warn("SMTP server is not configured"); //$NON-NLS-1$
			return false;
        }
		return true;
	}
	
	
	

}
