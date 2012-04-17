/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import com.triniforce.utils.IPropSerializabe;
import com.triniforce.utils.TFUtils;

public class PKArchiveDataEntry implements IPropSerializabe{

	private int m_dataType;
	public int getDataType() {
        return m_dataType;
    }

    public void setDataType(int dataType) {
        m_dataType = dataType;
    }

    public String getArchiveKey() {
        return m_archiveKey;
    }

    public void setArchiveKey(String archiveKey) {
        m_archiveKey = archiveKey;
    }

    private String m_archiveKey;
	
	public static String constructArchiveKey(int dataType, String extensionKey, String dataKey){
		//return "t" + dataType + "_" + extensionKey +"_" + dataKey;
		return extensionKey +"_" + dataKey;
	}
	
	public PKArchiveDataEntry(){
	}
	
	PKArchiveDataEntry(PKArchiveDataEntryKey ek, int dataType){
		TFUtils.assertTrue(PKArchiveMetaData.allowedDataTypes.contains(dataType), "Wrong dataType " + dataType);
		m_dataType = dataType;
		m_archiveKey = constructArchiveKey(dataType, ek.getExtensionKey(), ek.getDataKey());
	}

}