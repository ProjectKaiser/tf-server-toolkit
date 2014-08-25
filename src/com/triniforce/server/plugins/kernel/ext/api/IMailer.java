package com.triniforce.server.plugins.kernel.ext.api;


public interface IMailer {
	
	public void send(String from, String to, String subject, String body);

	public void send(String from, String to, String subject, String bodyType, String body, 
			String attachFile, String attachType, byte[] attachment);

}
