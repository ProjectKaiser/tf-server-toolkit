/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.sojo.common.ObjectUtil;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.EUtils;
import com.triniforce.utils.TFUtils;

public class PKArchiveMetaDataTest extends TFTestCase {
    @Override
    public void test() throws Exception {
        ObjectUtil ou = new ObjectUtil();
        PKArchiveMetaData md = new PKArchiveMetaData();
        try {
            md.putDataEntry("e1", "data", 2);
        } catch (EUtils.EAssertionFailed e) {
            trace(e);
        }
        try {
            md.putDataEntry(null, "data", 1);
        } catch (EUtils.EAssertionFailed e) {
            trace(e);
        }
        try {
            md.putDataEntry("e1", null, 1);
        } catch (EUtils.EAssertionFailed e) {
            trace(e);
        }

        md.putDataEntry("e1", "d1", 0);
        try {
            md.putDataEntry("e1", "d1", 0);
        } catch (EDataEntryAlreadyExists e) {
            trace(e);
        }
        md.putDataEntry("e1", "d2", 1);
        md.putDataEntry("e2", "d1", 1);
        PKArchiveDataEntry de = md.putDataEntry("e2", "d2", 0);
        assertNotNull(de.getArchiveKey());

        try {
            md.getDataEntry("e1", "d11", 0);
        } catch (EDataEntryNotFound e) {
            trace(e);
        }
        assertEquals(0, md.getDataEntry("e2", "d2", 0).getDataType());
        assertEquals(1, md.getDataEntry("e2", "d1", 1).getDataType());
        try {
            md.getDataEntry("e2", "d1", 0);
        } catch (EDataEntryWrongRequestedType e) {
            trace(e);
        }
        
        assertEquals(2, md.getExtensionIds().size());
        assertTrue(md.getExtensionIds().contains("e1"));
        assertTrue(md.getExtensionIds().contains("e2"));
        
        //save to folder
        {
            File folder = getTmpFolder(this);
            File zip = new File(folder, "archive.zip");
            ZipOutputStream zos = TFUtils.zipOpen(zip);
            md.serialize(zos);
            TFUtils.zipClose(zos);
            
            ZipFile zf = TFUtils.unzipOpen(zip);
            PKArchiveMetaData md2 = PKArchiveMetaData.deserialize(zf);
            TFUtils.unzipClose(zf);
            assertTrue(ou.equals(md, md2));
            assertEquals(2, md2.getExtensionIds().size());
            assertTrue(md2.getExtensionIds().contains("e1"));
            assertTrue(md2.getExtensionIds().contains("e2"));
        }
    }

}
