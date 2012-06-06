/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.ReverseIterator;

public abstract class PKBackupRestore {
	public static class InitedEntry{
		private final IPKEPBackupRestoreEntry m_bre;
		private final String m_extensionId;
		public InitedEntry(IPKEPBackupRestoreEntry bre, String extensionId) {
			m_bre = bre;
			m_extensionId = extensionId;
		}
		public IPKEPBackupRestoreEntry getBre() {
			return m_bre;
		}
		public String getExtensionId() {
			return m_extensionId;
		}
	}
	
	List<InitedEntry> m_inited = new ArrayList<InitedEntry>();
	TempFolders m_tempFolders = new TempFolders();
	
    final PKEPBackupRestore m_br;
    PKBackupRestore(PKEPBackupRestore br){
        m_br = br;
    }
    
    abstract void initEntry(InitedEntry ie);
    
    abstract Collection<String> getIdsToProcess();
    
    public void init_process_finit(){
        Object bs =  m_br.getRootExtensionPoint();
        if( bs instanceof IBasicServer){
            ApiStack.pushApi(((IBasicServer) bs).getCoreApi());
        }
        try {
            try {
                init();
                process();
            } finally {
                finit();
            }
        } finally {
            if(  bs instanceof IBasicServer){
            	ApiStack.popApi();
            }
        }
    }
    
    public void init(){
        Collection<String> ids = getIdsToProcess();
        for(String extensionId:m_br.getExtensions().keySet()){
            if(! ids.contains(extensionId)){
                continue;
            }
            IPKEPBackupRestoreEntry ibre = m_br.getExtension(extensionId).getInstance();
            InitedEntry ie = new InitedEntry(ibre, extensionId);
            initEntry(ie);
            m_inited.add(ie);
        }
    }
    
    abstract void processEntry(InitedEntry ie);
    
    public void process(){
        for(InitedEntry ie:m_inited){
            processEntry(ie);
        }
    }
    
    abstract void finitEntry(InitedEntry ie);
    
    public void finit(){
        for(InitedEntry ie: new ReverseIterator<InitedEntry>(m_inited)){
            try{
                finitEntry(ie);
            }catch(Throwable t){
                ApiAlgs.getLog(this).error("The following exception is ignored", t);
            }
        }
        m_tempFolders.finit();
    }
}
