/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.pkarchive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.StringSerializer;
import com.triniforce.utils.TFUtils;

@SuppressWarnings("serial")
public class PKArchiveMeta implements Serializable {

    public static final String META_TAG = "com.triniforce.server.plugins.kernel.pkarchive.PKArchiveMeta";

    transient ZipOutputStream m_zos;
    transient File m_outFile;
    transient ZipFile m_zf;

    ZipOutputStream getZOS() {
        TFUtils.assertNotNull(m_outFile, "Out file is not assigned");
        if (null == m_zos) {
            try {
                m_zos = new ZipOutputStream(new FileOutputStream(m_outFile));
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
        }
        return m_zos;
    }

    private String m_serKey = StringSerializer.PREFIX_JSON;
    
    public PKArchiveMeta() {
    }

    PKArchiveMeta(File outFile) {
        m_outFile = outFile;
    }

    public void serialize() {
        // for(Entry<String, Ext> e_ext: getExts().entrySet()){
        // Ext ext = e_ext.getValue();
        // for(Entry<String, ExtEntry> e_ee: ext.getExtEntries().entrySet()){
        // ExtEntry ee = e_ee.getValue();
        // String zipEntryName = e_ext.getKey() + "-" + e_ee.getKey();
        // if(null == ee.getZipEntryName()){
        // ee.setZipEntryName(zipEntryName);
        // }
        // ApiAlgs.getLog(this).trace("serialize " + zipEntryName);
        // TFUtils.assertNotNull(ee.m_extEntryDataHandler, "ext entry data: " +
        // zipEntryName);
        // TFUtils.assertNotNull(ee.m_extEntryDataHandler.m_data,
        // "ext entry data data: " + zipEntryName);
        // ee.serialize(getZOS());
        // }
        // }
        TFUtils.zipString(getZOS(), META_TAG, StringSerializer.object2String(this, getSerKey()));
        close();
    }

    public void close() {
        try {
            if (null != m_zos) {
                m_zos.close();
                m_zos = null;
            }
            if (null != m_zf) {
                m_zf.close();
                m_zf = null;
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

    public static PKArchiveMeta deserialize(File zipFile) {
        try {
            ZipFile zf = new ZipFile(zipFile);
            String data = TFUtils.unzipString(zf, META_TAG, true);
            PKArchiveMeta am = (PKArchiveMeta) StringSerializer.string2Object(data);
            am.m_zf = zf;
            return am;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

    Map<String, Ext> m_exts = new HashMap<String, Ext>();

    public Map<String, Ext> getExts() {
        return m_exts;
    }

    public static class EEntryNotFound extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EEntryNotFound(String extKey, String entryKey) {
            super(MessageFormat.format("Archive entry {0}/{1} not found",
                    extKey, entryKey));
        }
    }

    public ExtEntry getExtEntry(String extKey, String entryKey)
            throws EEntryNotFound {
        Ext ext = getExts().get(extKey);
        if (null == ext) {
            throw new EEntryNotFound(extKey, entryKey);
        }
        ExtEntry ee = ext.getExtEntries().get(entryKey);
        if (null == ee) {
            throw new EEntryNotFound(extKey, entryKey);
        }
        return ee;
    }

    public void serializeEntry(String extKey, String entryKey, Object data) {
        ExtEntry ee = getExtEntry(extKey, entryKey);
        String zipEntryName = extKey + "-" + entryKey;
        ApiAlgs.getLog(this).trace("serializing " + zipEntryName);
        if (null == ee.getZipEntryName()) {
            ee.setZipEntryName(zipEntryName);
        }
        ee.serialize(this, getZOS(), data);
    }

    public static class ENullZipEntryName extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ENullZipEntryName(String extKey, String entryKey) {
            super(MessageFormat.format("Bla blah={0}", extKey, entryKey));
        }
    }

    public Object deserializeEntry(String extKey, String entryKey, Object data)
            throws EEntryNotFound, ENullZipEntryName {       
        ExtEntry ee = getExtEntry(extKey, entryKey);
        if (null == ee.getZipEntryName()) {
            throw new ENullZipEntryName(extKey, entryKey);
        }
        ApiAlgs.getLog(this).trace("deserializing " + ee.getZipEntryName());
        return ee.deserialize(this, m_zf, data);
    }

    public void setExts(Map<String, Ext> exts) {
        m_exts = exts;
    }

    public void setSerKey(String serKey) {
        m_serKey = serKey;
    }

    public String getSerKey() {
        return m_serKey;
    }

}
