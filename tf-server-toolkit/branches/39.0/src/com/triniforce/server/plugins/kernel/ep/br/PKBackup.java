/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IPropSerializabe;

public class PKBackup extends PKBackupRestore{
    private final File m_archive;

	PKBackup(PKEPBackupRestore br, File archive){
        super(br);
		m_archive = archive;
    }
    
    List<IPKEPBackupRestoreEntry> m_inited = new ArrayList<IPKEPBackupRestoreEntry>();
    
    @Override
    void initEntry(InitedEntry ie) {
    	ie.getBre().initBackup();
    }

    @Override
    void finitEntry(InitedEntry ie) {
    	ie.getBre().finitBackup();
    }

    @Override
    public void process() {
    	PKArchiveWriter m_aw = new PKArchiveWriter(m_archive);
    	ApiStack.pushInterface(PKArchiveWriter.class, m_aw);
    	try{
    		super.process();
    		m_aw.writeMeta();
    	}finally{
    		ApiStack.popInterface(1);
    		m_aw.finit();
    		m_aw = null;
    	}
    }
    
	@Override
	void processEntry(InitedEntry ie){
		final InitedEntry loc_ie = ie;
		final PKArchiveWriter aw = ApiStack.getInterface(PKArchiveWriter.class);
		IBackupStorage bs = new IBackupStorage(){
			public File getTempFolder() {
				return m_tempFolders.getTempFolder();
			}

			public void writeFolder(String key, File folder) {
				aw.writeFolder(loc_ie.getExtensionId(), key, folder);
			}

			public void writeObject(String key, IPropSerializabe obj) {
				aw.writeObject(loc_ie.getExtensionId(), key, obj);
			}
		};
		ie.getBre().backup(bs);
	}

    @Override
    Collection<String> getIdsToProcess() {
        return m_br.getExtensions().keySet();
    }
}
