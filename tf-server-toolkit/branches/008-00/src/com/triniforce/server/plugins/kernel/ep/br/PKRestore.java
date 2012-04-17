/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;
import java.util.Collection;

public class PKRestore extends PKBackupRestore {

    IRestoreStorage getRestoreStorage(final InitedEntry ie){
        IRestoreStorage bs = new IRestoreStorage(){

            public File getTempFolder() {
                return m_tempFolders.getTempFolder();
            }

            public boolean queryKey(String key) {
                return null != m_ar.getMd().queryDataEntry(ie.getExtensionId(), key);
            }

            public Object readObject(String key) throws EObjectNotFound {
                return m_ar.readObject(ie.getExtensionId(), key);
            }

            public void restoreFolder(String key, File folder)
                    throws EObjectNotFound {
                m_ar.restoreFolder(ie.getExtensionId(), key, folder);
            }
        };
        return bs;
    }
    
    final PKArchiveReader m_ar;
    PKRestore(PKEPBackupRestore br, File archive){
        super(br);
        m_ar = new PKArchiveReader(archive);
    }
    
    @Override
    void finitEntry(InitedEntry ie){
    }

    @Override
    void initEntry(InitedEntry ie)  throws EEntryMayNotBeRestored {
        IRestoreStorage rs = getRestoreStorage(ie);
        String reason = ie.getBre().initRestore(rs);
        if(null != reason){
            throw new EEntryMayNotBeRestored(ie.getExtensionId(), reason);
        }
    }

    @Override
    void processEntry(InitedEntry ie) {
        ie.getBre().restore(getRestoreStorage(ie));
    }
    
    @Override
    public void finit() {
        m_ar.finit();
        super.finit();
    }

    @Override
    Collection<String> getIdsToProcess() {
        return m_ar.getMd().getExtensionIds();
    }
}
