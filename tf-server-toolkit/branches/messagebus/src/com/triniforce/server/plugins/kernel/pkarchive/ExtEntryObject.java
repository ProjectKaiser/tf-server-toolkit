/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.pkarchive;

import java.io.Serializable;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.triniforce.utils.StringSerializer;
import com.triniforce.utils.TFUtils;

@SuppressWarnings("serial")
public class ExtEntryObject extends ExtEntry{

    @Override
    protected Object deserialize(PKArchiveMeta am, ZipFile zf, Object data) {
        return StringSerializer.string2Object(TFUtils.unzipString(zf, getZipEntryName(), true));
    }

    @Override
    protected void serialize(PKArchiveMeta am, ZipOutputStream zos, Object data) {
        TFUtils.zipString(zos, getZipEntryName(), StringSerializer.object2String((Serializable)data, am.getSerKey()));
    }

}