/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.messagebus.error;

import com.triniforce.utils.IMessageHandler;

public class EPublicationError {

	// Internal state
	private Throwable cause;
	private String message;
	private IMessageHandler handler;
	private Object publishedMessage;

	/**
	 * Compound constructor, creating a PublicationError from the supplied objects.
	 * 
	 * @param cause
	 *            The Throwable giving rise to this PublicationError.
	 * @param message
	 *            The message to send.
	 * @param handler
	 *            The method where the error was created.
	 * @param listener
	 *            The object in which the PublicationError was generated.
	 * @param publishedObject
	 *            The published object which gave rise to the error.
	 */
	public EPublicationError(final Throwable cause, final String message, final IMessageHandler handler, final Object publishedObject) {

		this.cause = cause;
		this.message = message;
		this.handler = handler;
		this.publishedMessage = publishedObject;
	}

	public EPublicationError(Throwable cause, String message) {
		this.cause = cause;
		this.message = message;
	}

	/**
	 * Default constructor.
	 */
	public EPublicationError() {
		super();
	}

	/**
	 * @return The Throwable giving rise to this PublicationError.
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * Assigns the cause of this PublicationError.
	 * 
	 * @param cause
	 *            A Throwable which gave rise to this PublicationError.
	 * @return This PublicationError.
	 */
	public EPublicationError setCause(Throwable cause) {
		this.cause = cause;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public EPublicationError setMessage(String message) {
		this.message = message;
		return this;
	}

	public IMessageHandler getHandler() {
		return handler;
	}

	public EPublicationError setHandler(IMessageHandler handler) {
		this.handler = handler;
		return this;
	}


	public Object getPublishedMessage() {
		return publishedMessage;
	}

	public EPublicationError setPublishedMessage(Object publishedMessage) {
		this.publishedMessage = publishedMessage;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String newLine = System.getProperty("line.separator");
		return "EPublicationError{" + newLine + "\tcause=" + cause + newLine + "\tmessage='"
				+ message + '\'' + newLine + "\thandler=" + handler + newLine
				+ "\tpublishedMessage=" + publishedMessage + '}';
	}
}
