/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.triniforce.extensions.PluginsLoader;

/**
 * Location folder where jars should be loaded from
 */
public abstract class ClassesFolder{
    
    public ClassesFolder() {
    }
    
    Collection<Class> m_classes = null;

    public abstract File getFolder();
    
    synchronized Collection<Class> loadClasses(){
        PluginsLoader pl =  new PluginsLoader(getFolder());
        return pl.loadClasses();
    }
    
    Collection<Class> listClassesOfType(Class superClass){
        synchronized (this) {
            if (null == m_classes) {
                m_classes = loadClasses();
            }
        }
        
        Collection<Class> res =new ArrayList<Class>();
        
        for(Class cls: m_classes){
            if(superClass.isAssignableFrom(cls)){
                res.add(cls);
            }
        }
        return res;
    }

}
