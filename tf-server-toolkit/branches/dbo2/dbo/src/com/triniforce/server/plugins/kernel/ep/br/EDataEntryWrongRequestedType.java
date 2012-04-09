/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ep.br;

import java.text.MessageFormat;

public class EDataEntryWrongRequestedType extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public EDataEntryWrongRequestedType(String extensionKey, String entryKey,
            int requestedType, int actualType) {
        super(
                MessageFormat
                        .format(
                                "Wrong requested type of {0}.{1}. {2} requested but actual is {3}",
                                extensionKey, entryKey, requestedType,
                                actualType)
                                );
    }
}
