/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.pkarchive;

import java.io.File;
import java.io.Serializable;
import java.util.zip.ZipFile;

import net.sf.sojo.common.ObjectUtil;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.pkarchive.PKArchiveMeta.EEntryNotFound;
import com.triniforce.utils.StringSerializer;
import com.triniforce.utils.TFUtils;
import com.triniforce.utils.TFUtilsTest_Zip;

public class PKArchiveMetaTest extends TFTestCase {

    @SuppressWarnings("serial")
    public static class MyClass implements Serializable {
        String m_str1;

        public MyClass() {
        }

        public MyClass(String str1, String str2) {
            m_str1 = str1;
            m_str2 = str2;
        }

        public String getStr1() {
            return m_str1;
        }

        public void setStr1(String str1) {
            m_str1 = str1;
        }

        public String getStr2() {
            return m_str2;
        }

        public void setStr2(String str2) {
            m_str2 = str2;
        }

        String m_str2;
    }

    public void testSerKey() {
        File simple = new File(getTmpFolder(this), "simple.zip");
        PKArchiveMeta am = new PKArchiveMeta(simple);
        assertEquals(StringSerializer.PREFIX_JSON, am.getSerKey());
    }

    public void testGetEntry() {
        File simple = new File(getTmpFolder(this), "simple.zip");
        PKArchiveMeta am = new PKArchiveMeta(simple);
        am.getExts().put("ext1", new Ext());
        am.getExts().put("ext2", new Ext());
        try {
            am.getExtEntry("ext1", "k1");
            fail();
        } catch (EEntryNotFound e) {
            trace(e);
        }
        am.getExts().get("ext1").getExtEntries().put("k2", null);
        am.getExts().get("ext2").getExtEntries().put("k1", null);
        try {
            am.getExtEntry("ext1", "k1");
            fail();
        } catch (EEntryNotFound e) {
            trace(e);
        }
        am.getExts().get("ext1").getExtEntries()
                .put("k1", new ExtEntryObject());
        am.getExtEntry("ext1", "k1");
    }

    @Override
    public void test() throws Exception {
        ObjectUtil ou = new ObjectUtil();
        // test simple serialization
        {
            File simple = new File(getTmpFolder(this), "simple.zip");
            PKArchiveMeta am = new PKArchiveMeta(simple);
            am.getExts().put("ext1", new Ext());
            am.getExts().put("ext2", new Ext());
            am.serialize();

            {
                PKArchiveMeta am2 = new PKArchiveMeta(simple);
                assertFalse(ou.equals(am, am2));
            }
            PKArchiveMeta am3 = PKArchiveMeta.deserialize(simple);
            assertTrue(ou.equals(am, am3));
        }
        // test single object entry serialization
        {
            File ar = new File(getTmpFolder(this), "singleentry.zip");
            MyClass mc = new MyClass();
            mc.setStr1("str1 data");
            mc.setStr2("str2 data");

            {
                PKArchiveMeta am = new PKArchiveMeta(ar);
                am.getExts().put("ext1", new Ext());
                am.getExts().get("ext1").getExtEntries().put("data",
                        new ExtEntryObject());
                am.serializeEntry("ext1", "data", mc);
                am.serialize();
            }
            {
                PKArchiveMeta am = PKArchiveMeta.deserialize(ar);
                MyClass mc2 = (MyClass) am.deserializeEntry("ext1", "data",
                        null);
                assertTrue(ou.equals(mc, mc2));
            }
        }
    }

    public void tstExtEntryFolder(String serKey) throws Exception {
        ObjectUtil ou = new ObjectUtil();
        File archive = new File(getTmpFolder(this), "struct.zip");
        File struct = TFUtilsTest_Zip.prepareStructure(this, "struct");
        File outStruct = getTmpFolder(this, "outstruct");
        File outArchive = new File(getTmpFolder(this), "outstruct.zip");
        outStruct.mkdirs();
        MyClass mc = new MyClass("str1", "str2");
        {
            PKArchiveMeta am = new PKArchiveMeta(archive);
            am.setSerKey(serKey);
            am.getExts().put("ext1", new Ext());
            am.getExts().get("ext1").getExtEntries().put("k1",
                    new ExtEntryObject());
            am.getExts().get("ext1").getExtEntries().put("k2",
                    new ExtEntryFolder());
            am.serializeEntry("ext1", "k1", mc);
            am.serializeEntry("ext1", "k2", struct);
            am.serialize();
        }
        {
            PKArchiveMeta am = PKArchiveMeta.deserialize(archive);
            MyClass mc2 = (MyClass) am.deserializeEntry("ext1", "k1", null);
            assertTrue(ou.equals(mc, mc2));
            am.deserializeEntry("ext1", "k2", outStruct);
            TFUtils.zipFolder(outStruct, outArchive);
            ZipFile zf = TFUtils.unzipOpen(outArchive);
            TFUtilsTest_Zip.checkEntries(zf, "");
            TFUtils.unzipClose(zf);
        }

    }

    public void testExtEntryFolder1() throws Exception {
        tstExtEntryFolder(StringSerializer.PREFIX_JSON);
    }

    public void testExtEntryFolder2() throws Exception {
        tstExtEntryFolder(StringSerializer.PREFIX_BASE64);        
    }
}
