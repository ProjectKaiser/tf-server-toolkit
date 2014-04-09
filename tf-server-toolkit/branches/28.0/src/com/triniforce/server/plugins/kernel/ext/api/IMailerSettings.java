package com.triniforce.server.plugins.kernel.ext.api;

public interface IMailerSettings {
	
	public void loadSettings();
	
	public String getSmtpHost();
	public int getSmtpPort();
	public String getSmtpUser();
	public String getSmtpPassword();
	public boolean useTLS();

}
