/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

import com.triniforce.db.test.BasicServerRunningTestCase;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.extensions.PluginsLoaderTest;
import com.triniforce.server.srvapi.IPlugin;
import com.triniforce.utils.ApiStack;

public class PKEPExternalClassesTest extends BasicServerRunningTestCase {
    
    public void copyClasses(File dstFolder) throws Exception{
        copyTestResources(new String[]{"class1.jar", "class2.jar"}, dstFolder);
        
    }
    
    public static class ClassesFolder_All extends ClassesFolder{

        public ClassesFolder_All() {
        }
        
        @Override
        public File getFolder() {
            return folder1;
        }
        
    }
    public static class ClassesFolder_OnlyClasses extends ClassesFolder{

        @Override
        public File getFolder() {
            return folder2;
        }
    }
    
    public class MyPlugin extends PKPlugin{

        @Override
        public void doRegistration() {

            putExtension(PKEPExternalClasses.class, ClassesFolder_All.class);
            putExtension(PKEPExternalClasses.class, ClassesFolder_OnlyClasses.class);
            
        }

        @Override
        public void doExtensionPointsRegistration() {
        }
        
    }
    
    static File folder1;
    static File folder2;
    
    @Override
    protected void setUp() throws Exception {
        addPlugin(new MyPlugin());
        super.setUp();
        

    }
    
    
    @Override
    public void test() throws Exception {
        
        folder1 = new File(getTmpFolder(this), "f1");
        folder1.mkdirs();
        folder2 = new File(getTmpFolder(this), "f2");
        folder2.mkdirs();
        
        PluginsLoaderTest.copyTestPlugins(folder1);
        copyClasses(folder1);
        copyClasses(folder2);
        
       
        IExternalClasses ec = ApiStack.getInterface(IExternalClasses.class);
        //test all jars
        {
            Class fcls = ClassesFolder_All.class;
            Collection<Class> clss = ec.listClassesOfType(fcls, Object.class);
            assertEquals(4, clss.size());
            clss = ec.listClassesOfType(fcls, IPlugin.class);
            assertEquals(2, clss.size());
            clss = ec.listClassesOfType(fcls, Serializable.class);
            assertEquals(1, clss.size());
            clss = ec.listClassesOfType(fcls, Cloneable.class);
            assertEquals(1, clss.size());
        }
        {
            Class fcls = ClassesFolder_OnlyClasses.class;
            Collection<Class> clss = ec.listClassesOfType(fcls, IPlugin.class);
            assertEquals(0, clss.size());
            clss = ec.listClassesOfType(fcls, Serializable.class);
            assertEquals(1, clss.size());
            clss = ec.listClassesOfType(fcls, Cloneable.class);
            assertEquals(1, clss.size());        
        }

        

    }

}
