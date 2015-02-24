/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.text.MessageFormat;

public class EEntryMayNotBeRestored extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String m_extensionKey;
    private String m_entryKey;
    private final String m_reason;
    public EEntryMayNotBeRestored(String extensionKey, String reason) {
        super(MessageFormat.format("Entry {0} may not be restored: {1}", extensionKey, reason));
        m_extensionKey = extensionKey;
        m_reason = reason;
    }

    public String getExtensionKey() {
        return m_extensionKey;
    }

    public String getEntryKey() {
        return m_entryKey;
    }

    public String getReason() {
        return m_reason;
    }    

}
