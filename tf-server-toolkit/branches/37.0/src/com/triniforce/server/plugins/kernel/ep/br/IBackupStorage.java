/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;

import com.triniforce.utils.IPropSerializabe;

public interface IBackupStorage {
	void writeObject(String key, IPropSerializabe obj);
	void writeFolder(String key, File folder);
	File getTempFolder();
}
