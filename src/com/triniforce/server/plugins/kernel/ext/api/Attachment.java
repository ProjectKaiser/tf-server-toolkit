package com.triniforce.server.plugins.kernel.ext.api;

public class Attachment {
	private String fileName;
	private String contentType;
	private byte[] data;
	private String description;

	public Attachment() { }

	public Attachment(String fileName, String contentType, byte[] data, String description) {
		this.fileName = fileName;
		this.contentType = contentType;
		this.data = data;
		this.description = description;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
