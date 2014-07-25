package com.triniforce.server.plugins.kernel.ext.api;


public interface IMailer {
	
	public void send(String from, String to, String subject, String body);

}
