/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;

import net.sf.sojo.common.ObjectUtil;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.IPropSerializabe;
import com.triniforce.utils.TFUtilsTest_Zip;

public class PKArchiveWriterTest extends TFTestCase {
    
    public static class MyClass implements IPropSerializabe{
        String m_s1;
        String m_s2;
        public String getS1() {
            return m_s1;
        }
        public void setS1(String s1) {
            m_s1 = s1;
        }
        public String getS2() {
            return m_s2;
        }
        public void setS2(String s2) {
            m_s2 = s2;
        }
    }
    
    @Override
    public void test() throws Exception {
        ObjectUtil ou = new ObjectUtil();
        File folder = getTmpFolder(this);
        File archive = new File(folder, "archive.zip");
        File src = new File(folder, "src");
        File dst = new File(folder, "dst");
        TFUtilsTest_Zip.prepareStructure(this, src.getName());

        MyClass cls1 = new MyClass();
        cls1.setS1("cls1s1");
        cls1.setS2("cls1s2");
        MyClass cls2 = new MyClass();
        cls2.setS1("cls2s1");
        cls2.setS2("cls2s2");        
        
        
        //write
        {
            PKArchiveWriter pkw = new PKArchiveWriter(archive);
            pkw.writeObject("e1", "d1", cls1);
            pkw.writeObject("e1", "d2", cls2);
            pkw.writeFolder("e2", "d3", src);
            pkw.writeMeta();
            pkw.finit();
        }
        //read
        {
            PKArchiveReader pkr = new PKArchiveReader(archive);
            MyClass cls1_r = (MyClass) pkr.readObject("e1", "d1");
            MyClass cls2_r = (MyClass) pkr.readObject("e1", "d2");
            pkr.restoreFolder("e2", "d3", dst);
            pkr.finit();
            assertTrue(ou.equals(cls1, cls1_r));
            assertTrue(ou.equals(cls2, cls2_r));
            
            //test dst
            TFUtilsTest_Zip.checkEntries(dst);
        }
        
    }

}
