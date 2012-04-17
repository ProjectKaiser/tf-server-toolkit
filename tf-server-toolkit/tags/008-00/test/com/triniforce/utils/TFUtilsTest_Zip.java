/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.TFUtils.EZipEntryNotFound;

public class TFUtilsTest_Zip extends TFTestCase {

    public static void makeFile(File parent, String name) {
        parent.mkdirs();
        TFUtils.printlnToFile(new File(parent, name), "data " + name);
    }

    public void testZipClose(){
        TFUtils.zipClose(null);
    }
    
    public void testUnZipClose(){
        TFUtils.unzipClose(null);
    }
    
    public static File prepareStructure(File structRoot) {
        TFUtils.delTree(structRoot, true);
        structRoot.mkdirs();

        TFUtils.printlnToFile(new File(structRoot, "readme.txt"),
                "Experimental file");

        File dir1 = new File(structRoot, "dir1");
        makeFile(dir1, "file1-1");
        makeFile(dir1, "file1-2");
        File dir2 = new File(structRoot, "dir2");
        makeFile(dir2, "file2-1");
        makeFile(dir2, "file2-2");
        File dir3 = new File(structRoot, "dir3");
        dir3.mkdirs();
        return structRoot;
        
    }
    
    public static File prepareStructure(TFTestCase test, String folder) {
        File structRoot = getTmpFolder(test, folder);
        return prepareStructure(structRoot);
    }

    // String streamAsString(InputStream is){
    //	    	
    // }

    public static void checkEntry(ZipFile zf, String rootEntry, String entryName,
            String entryText) throws Exception {
        ZipEntry ze = zf.getEntry(rootEntry + entryName);
        assertNotNull(entryName, ze);
        if (null == entryText) {
            assertTrue(entryName, ze.isDirectory());
            return;
        }
        assertEquals(entryName, entryText, TFUtils.readStringFromStream(
                zf.getInputStream(ze), "utf-8", true).trim());
    }

    public static void checkEntries(File folder) throws Exception {
        File tmp = File.createTempFile("checkEntries", "");
        try{
            ZipOutputStream zos = TFUtils.zipOpen(tmp);
            try{
                TFUtils.zipFolderToEntryName(zos, "folder", folder);
            }finally{
                TFUtils.zipClose(zos);
            }
            ZipFile zf = TFUtils.unzipOpen(tmp);
            try{
                checkEntries(zf, "folder/");
            }finally{
                TFUtils.unzipClose(zf);
            }
        }finally{
            tmp.delete();
        }
        
    }
    
    public static void checkEntries(ZipFile zf, String rootEntry) throws Exception {
        checkEntry(zf, rootEntry, "readme.txt", "Experimental file");
        checkEntry(zf, rootEntry, "dir1/", null);
        checkEntry(zf, rootEntry, "dir1/file1-1", "data file1-1");
        checkEntry(zf, rootEntry, "dir1/file1-2", "data file1-2");
        checkEntry(zf, rootEntry, "dir2/", null);
        checkEntry(zf, rootEntry, "dir2/file2-1", "data file2-1");
        checkEntry(zf, rootEntry, "dir2/file2-2", "data file2-2");
        checkEntry(zf, rootEntry, "dir3/", null);
    }

    public void testStrings() throws Exception {
        File tmpFolder = getTmpFolder(this);
        File test = new File(tmpFolder, "test.zip");

        String UNICODE_PATTERN = "۞∑русскийڧüöäë面伴";
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(test));
        TFUtils.zipString(zos, "str1", "str1data");
        TFUtils.zipString(zos, "str2", UNICODE_PATTERN);
        zos.close();

        ZipFile zf = new ZipFile(test);
        assertEquals("str1data", TFUtils.unzipString(zf, "str1", false));
        assertEquals(UNICODE_PATTERN, TFUtils.unzipString(zf, "str2", false));
        assertNull(TFUtils.unzipString(zf, "str3", false));
        try {
            TFUtils.unzipString(zf, "str3", true);
            fail();
        } catch (EZipEntryNotFound e) {
            trace(e);
        }
        zf.close();

        TFUtils.delTree(tmpFolder, true);
    }

    @Override
    public void test() throws Exception {
        File struct = prepareStructure(this, "struct");

        {
            File zip = new File(getTmpFolder(this), "pr1.zip");
            TFUtils.zipFolder(struct, zip);
            ZipFile zf = new ZipFile(zip);
            checkEntries(zf, "");
            zf.close();
        }

        {
            File zip = new File(getTmpFolder(this), "pr2.zip");
            ZipOutputStream zout = TFUtils.zipOpen(zip);
            try {
                TFUtils.zipFolderToEntryName(zout, "com.triniforce", struct);
            } finally {
                zout.close();
            }

            {
                ZipFile zf = new ZipFile(zip);
                checkEntries(zf, "com.triniforce/");
                zf.close();
            }

            // unzip
            {
                File unzipFolder = getTmpFolder(this, "unzip");
                TFUtils.delTree(unzipFolder, true);
                {
                    ZipFile zf = new ZipFile(zip);
                    TFUtils.unzipEntry(zf, "com.triniforce/", unzipFolder);
                    zf.close();
                }

                // zip again
                File zip3 = new File(getTmpFolder(this), "pr3.zip");
                TFUtils.zipFolder(unzipFolder, zip3);
                ZipFile zf = new ZipFile(zip3);
                checkEntries(zf, "");
                zf.close();
            }
        }
    }
}
