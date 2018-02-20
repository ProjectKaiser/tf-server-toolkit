/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

public interface IPKEPBackupRestoreEntry {
    void initBackup();
    abstract void backup(IBackupStorage stg);
    void finitBackup();
    
    /**
     * @param stg
     * @return not null if entry may not be restored
     */
    String initRestore(IRestoreStorage stg);
    abstract void restore(IRestoreStorage stg);
    void finitRestore();
}
