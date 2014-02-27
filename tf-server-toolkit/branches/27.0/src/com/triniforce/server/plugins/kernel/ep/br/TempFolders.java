/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.TFUtils;

public class TempFolders {

    List<File> m_createdFolders = new ArrayList<File>();
    final File m_sysTempDir = new File(System.getProperty("java.io.tmpdir"));

    public TempFolders() {

    }

    public File getTempFolder() {
        try {
            final int maxAttempts = 9;
            int attemptCount = 0;
            File newTempDir;
            do {
                attemptCount++;
                if (attemptCount > maxAttempts) {
                    throw new IOException(
                            "The highly improbable has occurred! Failed to "
                                    + "create a unique temporary directory after "
                                    + maxAttempts + " attempts.");
                }
                String dirName = UUID.randomUUID().toString();
                newTempDir = new File(m_sysTempDir, dirName);
            } while (newTempDir.exists());
            if(newTempDir.mkdirs())
            {
                m_createdFolders.add(newTempDir);
                return newTempDir;
            }
            else
            {
                throw new IOException(
                        "Failed to create temp dir named " +
                        newTempDir.getAbsolutePath());
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

    /**
     * Kills all previously created folders returned by getTempFolder()
     */
    public void finit() {
        for(File dir:m_createdFolders){
            TFUtils.delTree(dir, true);
        }
    }

}
