/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.plugins.kernel.ep.br;


import java.io.File;

import com.triniforce.extensions.PKExtensionPoint;

/**
 * <b>PKBackupRestore</b> provides base algorythm, which is extended by <b>PKBackup/PKRestore</b>
 * <p>
 * Algorythms use <b>PKArchiveWriter</b> and <b>PKArchiveReader</b> classes which writes/read the archive.
 * 
 */
public class PKEPBackupRestore extends PKExtensionPoint {
	
	public PKEPBackupRestore() {
		setExtensionClass(IPKEPBackupRestoreEntry.class);
		setSingleExtensionInstances(false);
	}
	
    public void backup(File archive){
        PKBackup pb = new PKBackup(this, archive);
        pb.init_process_finit();
    }
    
    public void restore(File archive){
        PKRestore pb = new PKRestore(this, archive);
        pb.init_process_finit();
    }    
    
}
