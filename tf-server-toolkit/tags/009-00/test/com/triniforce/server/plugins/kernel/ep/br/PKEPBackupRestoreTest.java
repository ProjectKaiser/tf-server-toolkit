/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.plugins.kernel.ep.br.Entries.Entry1_MyClass1;
import com.triniforce.server.plugins.kernel.ep.br.Entries.Entry3_Folder_Args;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.TFUtilsTest_Zip;

public class PKEPBackupRestoreTest extends TFTestCase {
    File m_archive;
    File m_tmpFolder;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_tmpFolder = getTmpFolder(this);
        m_archive = new File(m_tmpFolder, "archive.zip");
    }
    
    public void testBackupRestoreEntry1_Entry3() throws Exception{
        PKEPBackupRestore br = new PKEPBackupRestore();
        final IPKEPBackupRestoreEntry bre1 = new Entries.Entry1_MyClass1();
        final IPKEPBackupRestoreEntry bre2 = new Entries.Entry3_Folder();
        br.putExtension("bre1", bre1);
        br.putExtension("bre2", bre2);
        Entry3_Folder_Args e3args = new Entry3_Folder_Args();
        e3args.folder1 = new File(m_tmpFolder, "folder1");
        e3args.folder2 = new File(m_tmpFolder, "folder2");
        e3args.folder1_out = new File(m_tmpFolder, "folder1_out");
        e3args.folder2_out = new File(m_tmpFolder, "folder3_out");
        TFUtilsTest_Zip.prepareStructure(this, e3args.folder1.getName());
        TFUtilsTest_Zip.prepareStructure(this, e3args.folder2.getName());
//        e3args.folder1_out.mkdirs();
//        e3args.folder2_out.mkdirs();
        TestProps tp = new TestProps();
        ApiStack.pushInterface(TestProps.class, tp);
        ApiStack.pushInterface(e3args.getClass(), e3args);
        try{
            br.backup(m_archive);
            br.restore(m_archive);
            {
                Entries.Entry1_MyClass1 emc1 = (Entry1_MyClass1) tp
                        .getObject(Entries.Entry1_MyClass1.class);
                emc1.assertEquals();
            }   
            //test folders
            {
                TFUtilsTest_Zip.checkEntries(e3args.folder1_out);
                TFUtilsTest_Zip.checkEntries(e3args.folder2_out);
            }
        }finally{
            ApiStack.popInterface(2);
        }
    }

}
