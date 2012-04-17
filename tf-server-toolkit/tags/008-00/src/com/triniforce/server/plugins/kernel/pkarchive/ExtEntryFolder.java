/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.pkarchive;

import java.io.File;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.triniforce.utils.TFUtils;

@SuppressWarnings("serial")
public class ExtEntryFolder extends ExtEntry{

    @Override
    protected Object deserialize(PKArchiveMeta am, ZipFile zf, Object data) {
    	TFUtils.unzipEntry(zf, getZipEntryName(), (File) data);
    	return data;
    }

    @Override
    protected void serialize(PKArchiveMeta am, ZipOutputStream zos, Object data) {
        TFUtils.zipFolderToEntryName(zos, getZipEntryName(), (File)data);
    }

}