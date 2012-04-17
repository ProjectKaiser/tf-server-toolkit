/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.pkarchive;

import java.io.Serializable;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("serial")
public abstract class ExtEntry implements Serializable{
    String m_zipEntryName;//name in zip archive
    String m_entryName;
    
    public String getZipEntryName() {
        return m_zipEntryName;
    }
    public void setZipEntryName(String zipEntryName) {
        m_zipEntryName = zipEntryName;
    }
  
    protected abstract void serialize(PKArchiveMeta am, ZipOutputStream zos, Object data);
    protected abstract Object deserialize(PKArchiveMeta am, ZipFile zf, Object data);

}