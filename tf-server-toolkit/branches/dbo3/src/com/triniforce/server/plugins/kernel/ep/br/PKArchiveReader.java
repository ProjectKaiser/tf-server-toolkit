/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;
import java.util.zip.ZipFile;

import com.triniforce.utils.IPropSerializabe;
import com.triniforce.utils.TFUtils;

public class PKArchiveReader {
    
    final ZipFile m_zf;
    private final PKArchiveMetaData m_md;
    
    public PKArchiveReader(File archive) {
        m_zf = TFUtils.unzipOpen(archive);
        m_md = PKArchiveMetaData.deserialize(m_zf);
    }
    
    public IPropSerializabe readObject(String extensionKey, String dataKey){
        PKArchiveDataEntry de = getMd().getDataEntry(extensionKey, dataKey, PKArchiveMetaData.DATA_TYPE_OBJECT);
        String str = TFUtils.unzipString(m_zf, de.getArchiveKey(), true);
        return (IPropSerializabe) PKArchiveMetaData.string2Object(str);
    }
    
    public void restoreFolder(String extensionKey, String dataKey, File folder){
        PKArchiveDataEntry de = getMd().getDataEntry(extensionKey, dataKey, PKArchiveMetaData.DATA_TYPE_FOLDER);
        TFUtils.delTree(folder, false);
        TFUtils.unzipEntry(m_zf, de.getArchiveKey(), folder);
    }
    
    public void finit(){
        TFUtils.unzipClose(m_zf);
    }

    public PKArchiveMetaData getMd() {
        return m_md;
    }

}
