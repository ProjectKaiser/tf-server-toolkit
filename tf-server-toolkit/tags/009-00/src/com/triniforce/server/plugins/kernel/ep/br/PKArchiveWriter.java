/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;
import java.util.zip.ZipOutputStream;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IPropSerializabe;
import com.triniforce.utils.TFUtils;

public class PKArchiveWriter{
    
    final ZipOutputStream m_zos;
    final PKArchiveMetaData m_md = new PKArchiveMetaData();
    
    public ZipOutputStream getZos() {
        return m_zos;
    }
    
    PKArchiveWriter(File archive){
        m_zos = TFUtils.zipOpen(archive);        
    }
    
    public void writeObject(String extensionKey, String dataKey, IPropSerializabe object){
        PKArchiveDataEntry de = m_md.putDataEntry(extensionKey, dataKey, PKArchiveMetaData.DATA_TYPE_OBJECT);
        TFUtils.zipString(getZos(), de.getArchiveKey(), PKArchiveMetaData.object2String(object));
    }
    public void writeFolder(String extensionKey, String dataKey, File folder){
        PKArchiveDataEntry de = m_md.putDataEntry(extensionKey, dataKey, PKArchiveMetaData.DATA_TYPE_FOLDER);
        TFUtils.zipFolderToEntryName(getZos(), de.getArchiveKey(), folder);
    }

    public void writeMeta(){
        m_md.serialize(m_zos);
    }
    
    public void finit(){
        try{
            TFUtils.zipClose(m_zos);
        }catch(Throwable t){
            ApiAlgs.getLog(this).error("Exception ignored", t);
        }
    }
    
}
