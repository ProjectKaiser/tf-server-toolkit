/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import java.text.MessageFormat;

public class EExtensionNotFound extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EExtensionNotFound(String extensionKey, IPKExtensionPoint ep){
        super(MessageFormat.format("Extension \"{0}\" not found in {1}", extensionKey, ep.getId()));
    }
}