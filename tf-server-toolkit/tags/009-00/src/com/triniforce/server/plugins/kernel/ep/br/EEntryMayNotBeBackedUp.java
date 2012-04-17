/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.text.MessageFormat;

public class EEntryMayNotBeBackedUp extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String m_extensionKey;
    private final String m_entryKey;

    public EEntryMayNotBeBackedUp(String extensionKey, String entryKey, String reason) {
        super(MessageFormat.format("Entry {0}.{1} may not be backed up: {2}", extensionKey, entryKey, reason));
        m_extensionKey = extensionKey;
        m_entryKey = entryKey;
    }

    public String getExtensionKey() {
        return m_extensionKey;
    }

    public String getEntryKey() {
        return m_entryKey;
    }
}