/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus.error;


public interface IPublicationErrorHandler {
	void handleError(EPublicationError error);
}
