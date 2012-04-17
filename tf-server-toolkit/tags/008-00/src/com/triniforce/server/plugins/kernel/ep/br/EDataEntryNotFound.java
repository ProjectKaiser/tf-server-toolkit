/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import java.text.MessageFormat;

public class EDataEntryNotFound extends RuntimeException {
	private static final long serialVersionUID = 1L;
    public EDataEntryNotFound(String extensionKey, String entryKey) {
        super(MessageFormat.format("Data entry {0}.{1} not found", extensionKey, entryKey));
    }

}
