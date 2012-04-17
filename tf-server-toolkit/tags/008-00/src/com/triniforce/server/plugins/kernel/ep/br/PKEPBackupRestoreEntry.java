/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

public abstract class PKEPBackupRestoreEntry implements IPKEPBackupRestoreEntry{
    
    public static final String KEY_DATA = "data";
	
	public void initBackup(){};
	public abstract void backup(IBackupStorage stg);
	public void finitBackup(){};
	
	public String initRestore(IRestoreStorage stg){return null;};
	public abstract void restore(IRestoreStorage stg);
	public void finitRestore(){};
}
