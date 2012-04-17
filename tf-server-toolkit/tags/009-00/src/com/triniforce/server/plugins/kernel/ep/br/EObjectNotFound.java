/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import java.text.MessageFormat;

public class EObjectNotFound extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public EObjectNotFound(String entryKey, String objectKey) {
		super(MessageFormat.format("Object with key {1} not found for entry {0}", entryKey, objectKey));
	}
}
