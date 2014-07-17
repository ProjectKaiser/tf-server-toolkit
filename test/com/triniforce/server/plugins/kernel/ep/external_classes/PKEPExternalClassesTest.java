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
import com.triniforce.extensions.IPKExtensionPoint;
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
    
    
    @Override
    public void test() throws Exception {
        
        File folder1 = new File(getTmpFolder(this), "f1");
        folder1.mkdirs();
        File folder2 = new File(getTmpFolder(this), "f2");
        folder2.mkdirs();
        
        PluginsLoaderTest.copyTestPlugins(folder1);
        PluginsLoaderTest.copyClasses(folder2);

        ClassesFolder cfAll = new ClassesFolder_All(folder1);
        ClassesFolder cfOnlyClasses = new ClassesFolder_OnlyClasses(folder2);
        
        IPKExtensionPoint ep = m_server.getExtensionPoint(PKEPExternalClasses.class);
        ep.putExtension(cfAll);
        ep.putExtension(cfOnlyClasses);
        
        IExternalClasses ec = ApiStack.getInterface(IExternalClasses.class);
        //test all jars
        {
            Class fcls = ClassesFolder_All.class;
            Collection<Class> cls = ec.listClassesOfType(fcls, Object.class);
            assertEquals(4, cls.size());
            cls = ec.listClassesOfType(fcls, IPlugin.class);
            assertEquals(2, cls.size());
            cls = ec.listClassesOfType(fcls, Serializable.class);
            assertEquals(1, cls.size());
            cls = ec.listClassesOfType(fcls, Cloneable.class);
            assertEquals(1, cls.size());
        }
        {
            Class fcls = ClassesFolder_OnlyClasses.class;
            Collection<Class> cls = ec.listClassesOfType(fcls, IPlugin.class);
            assertEquals(0, cls.size());
            cls = ec.listClassesOfType(fcls, Serializable.class);
            assertEquals(1, cls.size());
            cls = ec.listClassesOfType(fcls, Cloneable.class);
            assertEquals(1, cls.size());        
        }

        

    }

}
