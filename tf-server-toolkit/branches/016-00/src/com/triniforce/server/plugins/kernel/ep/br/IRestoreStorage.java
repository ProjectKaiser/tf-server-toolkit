/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;

public interface IRestoreStorage {
	File getTempFolder();
	boolean queryKey(String key);
    Object readObject(String key) throws EObjectNotFound;
	void restoreFolder(String key, File folder) throws EObjectNotFound;;
}
