package com.triniforce.server.plugins.kernel.ext.api;


public interface IMailer {
	static class EMailerConfigurationError extends Exception{
		private static final long serialVersionUID = -2931102442738779348L;
	}
	
	public boolean isMailerConfigured();
	
	public boolean send(String from, String to, String subject, String body) throws EMailerConfigurationError;

	public boolean send(String from, String to, String subject, String bodyType, String body, 
			String attachFile, String attachType, byte[] attachment) throws EMailerConfigurationError;

}
