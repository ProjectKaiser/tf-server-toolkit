package com.triniforce.server.plugins.kernel.ext.api;

import javax.mail.Session;

public interface IMailerSettings {
	
	/**
	 * @return Null if session factory is not configured
	 */
	public Session createSmtpSession();
	
	public void loadSettings();
	
	public String getSmtpHost();
	public int getSmtpPort();
	public String getSmtpUser();
	public String getSmtpPassword();
	public boolean useTLS();
	public String getDefaultSender();

}
