/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.ep.br;

import com.triniforce.utils.TFUtils;


public class PKArchiveDataEntryKey {
	private final String m_extensionKey;
	private final String m_dataKey;

	
	public PKArchiveDataEntryKey(String extensionKey, String dataKey){
		TFUtils.assertNotNull(extensionKey, "extensionKey");
		TFUtils.assertNotNull(dataKey, "dataKey");
		m_extensionKey = extensionKey;
		m_dataKey = dataKey;
	}

	public String toStringKey(){
		//do NOT change "-" since it is serialized
	    return m_extensionKey + "-" + m_dataKey;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((m_dataKey == null) ? 0 : m_dataKey.hashCode());
		result = prime
				* result
				+ ((m_extensionKey == null) ? 0 : m_extensionKey.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PKArchiveDataEntryKey other = (PKArchiveDataEntryKey) obj;
		if (m_dataKey == null) {
			if (other.m_dataKey != null)
				return false;
		} else if (!m_dataKey.equals(other.m_dataKey))
			return false;
		if (m_extensionKey == null) {
			if (other.m_extensionKey != null)
				return false;
		} else if (!m_extensionKey.equals(other.m_extensionKey))
			return false;
		return true;
	}
	
	public String getExtensionKey() {
		return m_extensionKey;
	}

	public String getDataKey() {
		return m_dataKey;
	}
}
