/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ep.br;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.triniforce.utils.IPropSerializabe;
import com.triniforce.utils.StringSerializer;
import com.triniforce.utils.TFUtils;

public class PKArchiveMetaData implements IPropSerializabe {
    
    public static String object2String(IPropSerializabe obj){
        return StringSerializer.rawObject2String(obj, StringSerializer.PREFIX_JSON);
    }
    
    public static Object string2Object(String str){
        return StringSerializer.rawString2Object(str, StringSerializer.PREFIX_JSON);
    }
    
    public static final int DATA_TYPE_OBJECT = 0;
    public static final int DATA_TYPE_FOLDER = 1;
    {
        allowedDataTypes.add(DATA_TYPE_OBJECT);
        allowedDataTypes.add(DATA_TYPE_FOLDER);
    }
    public static final Set<Integer> allowedDataTypes = new HashSet<Integer>();
    Map<String, PKArchiveDataEntry> m_dataEntries = new HashMap<String, PKArchiveDataEntry>();
    Set<String> m_extensionIds = new HashSet<String>();


	public Set<String> getExtensionIds() {
		return m_extensionIds;
	}

	public void setExtensionIds(Set<String> extensionIds) {
		m_extensionIds = extensionIds;
	}

	public Map<String, PKArchiveDataEntry> getDataEntries() {
        return m_dataEntries;
    }

    public static final String META_ENTRY_NAME = "PKArchiveMetaData";
    public void serialize(ZipOutputStream zos){
        String str = object2String(this);
        TFUtils.zipString(zos, META_ENTRY_NAME, str);
    }

    public static PKArchiveMetaData deserialize(ZipFile zf){
        String str = TFUtils.unzipString(zf, META_ENTRY_NAME, true);
        return (PKArchiveMetaData) string2Object(str);
    }
    
    public void setDataEntries(
            Map<String, PKArchiveDataEntry> dataEntries) {
        m_dataEntries = dataEntries;
    }

    public PKArchiveDataEntry putDataEntry(String extensionKey, String dataKey,
            int dataType) throws EDataEntryAlreadyExists {
        PKArchiveDataEntryKey ek = new PKArchiveDataEntryKey(extensionKey,
                dataKey);
        PKArchiveDataEntry ade = new PKArchiveDataEntry(ek, dataType);
        if (m_dataEntries.containsKey(ek)) {
            throw new EDataEntryAlreadyExists(extensionKey, dataKey);
        }
        m_dataEntries.put(ek.toStringKey(), ade);
        m_extensionIds.add(extensionKey);
        return ade;
    }

    public PKArchiveDataEntry getDataEntry(String extensionKey, String dataKey,
            int dataType) throws EDataEntryNotFound, EDataEntryWrongRequestedType {
        PKArchiveDataEntry de = queryDataEntry(extensionKey, dataKey);
        if (null == de) {
            throw new EDataEntryNotFound(extensionKey, dataKey);
        }
        if(! TFUtils.equals(dataType, de.getDataType())){
            throw new EDataEntryWrongRequestedType(extensionKey, dataKey, dataType, de.getDataType());            
        }
        return de;
    }

    public PKArchiveDataEntry queryDataEntry(String extensionKey, String dataKey)
            throws EDataEntryNotFound {
        PKArchiveDataEntryKey ek = new PKArchiveDataEntryKey(extensionKey,
                dataKey);
        return m_dataEntries.get(ek.toStringKey());
    }

}
