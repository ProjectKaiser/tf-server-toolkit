/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ep.br;

import java.io.File;

import junit.framework.TestCase;
import net.sf.sojo.common.ObjectUtil;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IPropSerializabe;
import com.triniforce.utils.TFUtilsTest_Zip;

public class Entries {
    public static class MyClass1 implements IPropSerializabe {
        private String m_str1;
        private String m_str2;

        public void setStr1(String str1) {
            m_str1 = str1;
        }

        public String getStr1() {
            return m_str1;
        }

        public void setStr2(String str2) {
            m_str2 = str2;
        }

        public String getStr2() {
            return m_str2;
        }
    }

    public static class MyClass2 implements IPropSerializabe{
        private Integer m_i1;
        private Integer m_i2;

        public void setI1(Integer i1) {
            m_i1 = i1;
        }

        public Integer getI1() {
            return m_i1;
        }

        public void setI2(Integer i2) {
            m_i2 = i2;
        }

        public Integer getI2() {
            return m_i2;
        }
    }

    public static class Entry1_MyClass1 extends PKEPBackupRestoreEntry {
        public MyClass1 m_mc1;
        
        @Override
        public void backup(IBackupStorage stg) {
            MyClass1 mc1 = new MyClass1();
            mc1.setStr1("string1");
            mc1.setStr2("string2");
            stg.writeObject("mc1", mc1);
        }
        
        @Override
        public void restore(IRestoreStorage stg) {
            m_mc1 = (MyClass1) stg.readObject("mc1");
            TestProps tp = ApiStack.getInterface(TestProps.class);
            tp.putObject(this);
        }
        
        public void assertEquals(){
            ObjectUtil ou = new ObjectUtil(); 
            MyClass1 mc1 = new MyClass1();
            mc1.setStr1("string1");
            mc1.setStr2("string2");
            TestCase.assertTrue(ou.equals(mc1, m_mc1));
        }
        
    }

    public static class Entry2_MyClass1_MyClass2 extends PKEPBackupRestoreEntry {
        public MyClass1 m_mc1;
        public MyClass2 m_mc2;
        @Override
        public void backup(IBackupStorage stg) {
            {
                MyClass1 mc1 = new MyClass1();
                mc1.setStr1("string_1");
                mc1.setStr2("string_2");
                stg.writeObject("mc_1", mc1);
            }
            {
                MyClass2 mc2 = new MyClass2();
                mc2.setI1(11);
                mc2.setI2(22);
                stg.writeObject("mc2", mc2);
            }            
        }

        @Override
        public void restore(IRestoreStorage stg) {
            m_mc1 = (MyClass1) stg.readObject("mc_1");
            m_mc2 = (MyClass2) stg.readObject("mc2");
            TestProps tp = ApiStack.getInterface(TestProps.class);
            tp.putObject(this);
        }
        public void assertEquals(){
            ObjectUtil ou = new ObjectUtil(); 
            MyClass1 mc1 = new MyClass1();
            mc1.setStr1("string_1");
            mc1.setStr2("string_2");
            MyClass2 mc2 = new MyClass2();
            mc2.setI1(11);
            mc2.setI2(22);
            
            TestCase.assertTrue(ou.equals(mc1, m_mc1));
            TestCase.assertTrue(ou.equals(mc2, m_mc2));
        }
    }
    
    public static class Entry3_Folder_Args{
        public File folder1;
        public File folder2;
        public File folder1_out;
        public File folder2_out;        
    }
    
    public static class Entry3_Folder  extends PKEPBackupRestoreEntry{
        @Override
        public void backup(IBackupStorage stg) {
            Entry3_Folder_Args args = ApiStack.getInterface(Entry3_Folder_Args.class);
            stg.writeFolder("folder1", args.folder1);
            stg.writeFolder("folder2", args.folder2);
        }

        @Override
        public void restore(IRestoreStorage stg) {
            Entry3_Folder_Args args = ApiStack.getInterface(Entry3_Folder_Args.class);
            stg.restoreFolder("folder1", args.folder1_out);
            stg.restoreFolder("folder2", args.folder2_out);
        }
    }
    
    public static class Entry4_TempFolder  extends PKEPBackupRestoreEntry{
        @Override
        public void backup(IBackupStorage stg) {
            File folder = stg.getTempFolder();
            TFUtilsTest_Zip.prepareStructure(folder);
            stg.writeFolder("folder", folder);
        }

        @Override
        public void restore(IRestoreStorage stg) {
            File folder = stg.getTempFolder();
            stg.restoreFolder("folder", folder);
            try {
                TFUtilsTest_Zip.checkEntries(folder);
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }            
        }
    }

}
