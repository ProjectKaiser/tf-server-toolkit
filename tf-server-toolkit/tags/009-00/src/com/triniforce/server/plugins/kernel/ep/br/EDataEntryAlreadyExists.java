/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import java.text.MessageFormat;

public class EDataEntryAlreadyExists extends RuntimeException{
	private static final long serialVersionUID = 1L;
    public EDataEntryAlreadyExists(String extensionKey, String entryKey) {
        super(MessageFormat.format("Data entry {0}.{1} already exists", extensionKey, entryKey));
    }
}
