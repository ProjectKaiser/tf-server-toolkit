/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.extensions;

import java.text.MessageFormat;

public class EExtensionPointNotFound extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EExtensionPointNotFound(String extensionPointId, IPKRootExtensionPoint ep){
        super(MessageFormat.format("Extension point \"{0}\" not found in {1}", extensionPointId, ep.getClass().getName()));
    }
}