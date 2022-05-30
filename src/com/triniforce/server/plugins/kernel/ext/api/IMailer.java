package com.triniforce.server.plugins.kernel.ext.api;

import java.io.Serializable;

public interface IMailer {
	static class EMailerConfigurationError extends Exception{
		private static final long serialVersionUID = -2931102442738779348L;
	}
	
	public static class Attachment implements Serializable {
		private static final long serialVersionUID = -5258299556395660257L;
		private String fileName;
		private String contentType;
		private byte[] data;
		private String description;

		public Attachment(String fileName, String contentType, byte[] data, String description) {
			this.fileName = fileName;
			this.contentType = contentType;
			this.data = data;
			this.description = description;
		}

		public String getFileName() {
			return fileName;
		}

		public String getContentType() {
			return contentType;
		}

		public byte[] getData() {
			return data;
		}

		public String getDescription() {
			return description;
		}
	}

	public boolean isMailerConfigured();
	
	public boolean send(String from, String to, String subject, String body) throws EMailerConfigurationError;

	public boolean send(String from, String to, String subject, String bodyType, String body, 
			String attachFile, String attachType, byte[] attachment) throws EMailerConfigurationError;

	public boolean send(String from, String to, String subject, String bodyType, String body, 
			Attachment[] attachments, IMailerSettings customMailerSettings) throws EMailerConfigurationError;

}
