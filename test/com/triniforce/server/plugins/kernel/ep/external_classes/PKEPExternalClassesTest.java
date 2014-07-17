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
    
    public static class ClassesFolder_All extends ClassesFolder{
        public ClassesFolder_All(File folder) {
            super(folder);
        }
    }
    public static class ClassesFolder_OnlyClasses extends ClassesFolder{
        public ClassesFolder_OnlyClasses(File folder){
            super(folder);
        }
    }
    
    public class MyPlugin extends PKPlugin{

        @Override
        public void doRegistration() {

            ClassesFolder cfAll = new ClassesFolder_All(folder1);
            ClassesFolder cfOnlyClasses = new ClassesFolder_OnlyClasses(folder2);
            putExtension(PKEPExternalClasses.class, cfAll.getClass().getName(), cfAll);
            putExtension(PKEPExternalClasses.class, cfOnlyClasses.getClass().getName(), cfOnlyClasses);
            
        }

        @Override
        public void doExtensionPointsRegistration() {
        }
        
    }
    
    File folder1;
    File folder2;
    
    @Override
    protected void setUp() throws Exception {
        addPlugin(new MyPlugin());
        folder1 = new File(getTmpFolder(this), "f1");
        folder1.mkdirs();
        folder2 = new File(getTmpFolder(this), "f2");
        folder2.mkdirs();
        PluginsLoaderTest.copyTestPlugins(folder1);
        PluginsLoaderTest.copyClasses(folder2);
        
        super.setUp();
        

    }
    
    
    @Override
    public void test() throws Exception {
        
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
