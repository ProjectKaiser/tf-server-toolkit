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
public class ClassesFolder{
    
    private final File m_folder;
    Collection<Class> m_classes = null;

    public ClassesFolder(File folder){
        m_folder = folder;
    }
    
    synchronized void loadClasses(){
        PluginsLoader pl =  new PluginsLoader(m_folder);
        m_classes = pl.loadClasses();
    }
    
    Collection<Class> listClassesOfType(Class superClass){
        if(null == m_classes){
            loadClasses();
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
